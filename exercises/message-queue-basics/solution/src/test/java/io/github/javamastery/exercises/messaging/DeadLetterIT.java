package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.resps.StreamEntry;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.github.javamastery.exercises.messaging.StreamFailurePolicy.FailureKind;
import static io.github.javamastery.exercises.messaging.StreamFailurePolicy.Outcome;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DeadLetterIT {
    private final String stream = "learning:mq:dead-it:" + UUID.randomUUID();
    private final Set<String> ownedKeys = new LinkedHashSet<>();
    private JedisPooled redis;
    private JedisPooled observer;
    private CourseEventQueue queue;
    private StreamFailurePolicy policy;
    private InboxFixture fixture;
    private ResilientNotificationConsumer consumer;

    @BeforeEach
    void connect() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        redis = new JedisPooled(host, port);
        observer = new JedisPooled(host, port);
        ownedKeys.add(stream);
        queue = new CourseEventQueue(redis, stream, "notifications");
        queue.ensureGroup();
        policy = policyFor(redis, queue, 3);
        fixture = new InboxFixture();
        consumer = new ResilientNotificationConsumer(queue, fixture.inbox, policy);
    }

    @AfterEach
    void cleanOwnResources() {
        try {
            if (observer != null) {
                observer.del(ownedKeys.toArray(String[]::new));
            }
        } finally {
            try {
                if (fixture != null) {
                    fixture.close();
                }
            } finally {
                if (observer != null) {
                    observer.close();
                }
                if (redis != null) {
                    redis.close();
                }
            }
        }
    }

    @Test
    void permanentPoisonIsPreservedInDeadLetterWhileHealthyMessageContinues() {
        StreamEntryID badId = poison();
        queue.publish(event());
        var result = consumer.processBatch("worker", queue.readNew("worker", 10));
        assertEquals(1, result.deadLettered());
        assertEquals(1, result.applied());
        assertEquals(1, fixture.notifications());
        assertEquals(0, queue.pendingCount());
        Map<String, String> fields = deadLetters(policy).getFirst().getFields();
        assertEquals(badId.toString(), fields.get("sourceId"));
        assertEquals(stream, fields.get("sourceStream"));
        assertEquals("notifications", fields.get("sourceGroup"));
        assertEquals("INVALID_EVENT", fields.get("errorCode"));
        assertEquals("unsupported", fields.get("payload.schemaVersion"));
        assertEquals("0", fields.get("payloadMissing"));
    }

    @Test
    void retryLimitAndTerminalReplayDoNotCreateDuplicateDeadLetters() {
        CoursePublishedEvent event = event();
        StreamEntryID id = queue.publish(event);
        List<QueuedMessage> messages = queue.readNew("worker", 1);
        NotificationInbox failing = spy(fixture.inbox);
        doThrow(new TransientDataAccessResourceException("temporary database failure")).when(failing).storeIfNew(event);
        StreamFailurePolicy twoFailures = policyFor(redis, queue, 2);
        ResilientNotificationConsumer retrying = new ResilientNotificationConsumer(queue, failing, twoFailures);
        assertEquals(1, retrying.processBatch("worker", messages).retryPending());
        assertEquals(1, queue.pendingCount());
        assertEquals(1, retrying.processBatch("worker", queue.readOwnPending("worker", 1)).deadLettered());
        assertEquals(0, queue.pendingCount());
        assertEquals(1, retrying.processBatch("worker", messages).alreadyDeadLettered());
        var repeated = twoFailures.recordFailure("worker", id, FailureKind.TRANSIENT_DATABASE);
        assertEquals(Outcome.ALREADY_DEAD_LETTERED, repeated.outcome());
        assertEquals(2, repeated.failures());
        assertEquals(1, redis.xlen(twoFailures.deadLetterStream()));
        assertEquals(repeated.deadLetterId(), deadLetters(twoFailures).getFirst().getID().toString());
        verify(failing, times(2)).storeIfNew(event);
        assertEquals(0, fixture.receipts());
    }

    @Test
    void invalidDeadLetterKeyTypeLeavesOriginalPendingAndUncounted() {
        poison();
        List<QueuedMessage> messages = queue.readNew("worker", 1);
        redis.set(policy.deadLetterStream(), "wrong type");
        assertThrows(JedisDataException.class, () -> consumer.processBatch("worker", messages));
        assertEquals(1, queue.pendingCount());
        assertFalse(redis.exists(policy.failureStateKey()));
        assertEquals(0, fixture.receipts());
        redis.del(policy.deadLetterStream());
        assertEquals(1, consumer.processBatch("worker", messages).deadLettered());
        assertEquals(0, queue.pendingCount());
    }

    @Test
    void previousOwnerCannotDeadLetterClaimedMessage() {
        poison();
        List<QueuedMessage> original = queue.readNew("original", 1);
        var claimed = queue.claimIdle("replacement", Duration.ZERO, new StreamEntryID("0-0"), 10);
        JedisDataException failure = assertThrows(JedisDataException.class,
                () -> consumer.processBatch("original", original));
        assertTrue(failure.getMessage().contains("OWNERSHIP_LOST"));
        assertFalse(redis.exists(policy.failureStateKey()));
        assertEquals(1, consumer.processBatch("replacement", claimed.messages()).deadLettered());
        assertEquals(0, queue.pendingCount());
    }

    @Test
    void acknowledgementFailureAfterCommitIsNotCountedAsPoison() {
        CoursePublishedEvent event = event();
        queue.publish(event);
        List<QueuedMessage> messages = queue.readNew("worker", 1);
        NotificationInbox closing = spy(fixture.inbox);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            redis.close();
            return result;
        }).when(closing).storeIfNew(event);
        var interrupted = new ResilientNotificationConsumer(queue, closing, policy);
        assertThrows(JedisException.class, () -> interrupted.processBatch("worker", messages));
        assertEquals(1, fixture.notifications());
        assertFalse(observer.exists(policy.failureStateKey()));
        assertFalse(observer.exists(policy.deadLetterStream()));
        CourseEventQueue recoveredQueue = new CourseEventQueue(observer, stream, "notifications");
        var recovered = new ResilientNotificationConsumer(recoveredQueue, fixture.inbox,
                policyFor(observer, recoveredQueue, 3));
        assertEquals(1, recovered.processBatch("worker", recoveredQueue.readOwnPending("worker", 10)).duplicates());
        assertEquals(1, fixture.notifications());
        assertEquals(0, recoveredQueue.pendingCount());
    }

    @Test
    void successfulRetryClearsFailureState() {
        CoursePublishedEvent event = event();
        queue.publish(event);
        NotificationInbox flaky = spy(fixture.inbox);
        doThrow(new TransientDataAccessResourceException("temporary failure")).doCallRealMethod()
                .when(flaky).storeIfNew(event);
        var recovering = new ResilientNotificationConsumer(queue, flaky, policy);
        assertEquals(1, recovering.processBatch("worker", queue.readNew("worker", 1)).retryPending());
        assertEquals("1", redis.hget(policy.failureStateKey(), queue.readOwnPending("worker", 1).getFirst().id() + ":attempts"));
        assertEquals(1, recovering.processBatch("worker", queue.readOwnPending("worker", 1)).applied());
        assertEquals(0, redis.hlen(policy.failureStateKey()));
        assertFalse(redis.exists(policy.deadLetterStream()));
        assertEquals(0, queue.pendingCount());
    }

    @Test
    void retryAndDeadLetterStateAreIsolatedByConsumerGroup() {
        CourseEventQueue other = new CourseEventQueue(observer, stream, "other-group");
        other.ensureGroup();
        StreamFailurePolicy otherPolicy = policyFor(observer, other, 3);
        poison();
        List<QueuedMessage> first = queue.readNew("worker", 1);
        List<QueuedMessage> second = other.readNew("worker", 1);
        assertEquals(1, consumer.processBatch("worker", first).deadLettered());
        assertEquals(1, other.pendingCount());
        assertFalse(observer.exists(otherPolicy.deadLetterStream()));
        NotificationInbox otherInbox = new NotificationInbox(fixture.jdbc, fixture.transactions, "other-group");
        assertEquals(1, new ResilientNotificationConsumer(other, otherInbox, otherPolicy)
                .processBatch("worker", second).deadLettered());
        assertEquals(1, redis.xlen(policy.deadLetterStream()));
        assertEquals(1, observer.xlen(otherPolicy.deadLetterStream()));
    }

    @Test
    void deletedPendingPayloadProducesAuditableMetadataOnlyDeadLetter() {
        StreamEntryID id = queue.publish(event());
        queue.readNew("worker", 1);
        redis.xdel(stream, id);
        assertEquals(1, consumer.processBatch("worker", queue.readOwnPending("worker", 1)).deadLettered());
        Map<String, String> fields = deadLetters(policy).getFirst().getFields();
        assertEquals(id.toString(), fields.get("sourceId"));
        assertEquals("1", fields.get("payloadMissing"));
        assertFalse(fields.containsKey("payload.title"));
        assertEquals(0, queue.pendingCount());
    }

    private StreamFailurePolicy policyFor(JedisPooled client, CourseEventQueue source, int maximumFailures) {
        StreamFailurePolicy created = new StreamFailurePolicy(client, source, maximumFailures);
        ownedKeys.add(created.failureStateKey());
        ownedKeys.add(created.deadLetterStream());
        return created;
    }

    private StreamEntryID poison() {
        return redis.xadd(stream, StreamEntryID.NEW_ENTRY, Map.of("schemaVersion", "unsupported"));
    }

    private CoursePublishedEvent event() {
        return new CoursePublishedEvent(UUID.randomUUID(), 1, "Healthy Event");
    }

    private List<StreamEntry> deadLetters(StreamFailurePolicy source) {
        return observer.xrange(source.deadLetterStream(), StreamEntryID.MINIMUM_ID, StreamEntryID.MAXIMUM_ID);
    }
}
