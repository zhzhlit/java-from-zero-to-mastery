package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class PublicationOutboxIT {
    private final String stream = "learning:mq:publication-it:" + UUID.randomUUID();
    private JedisPooled redis;
    private PublicationFixture fixture;
    private CourseEventQueue queue;
    private PublicationRelay relay;

    @BeforeEach
    void connect() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        redis = new JedisPooled(host, port);
        fixture = new PublicationFixture(stream);
        queue = new CourseEventQueue(redis, stream, "notifications");
        queue.ensureGroup();
        relay = new PublicationRelay(fixture.outbox, queue);
    }

    @AfterEach
    void cleanOwnResources() {
        try {
            if (redis != null) {
                redis.del(stream);
            }
        } finally {
            try {
                if (fixture != null) {
                    fixture.close();
                }
            } finally {
                if (redis != null) {
                    redis.close();
                }
            }
        }
    }

    @Test
    void committedEventFlowsThroughOutboxStreamAndInbox() {
        CoursePublishedEvent event = fixture.service.publish(1, "Published Course");
        assertEquals(0, redis.xlen(stream));
        assertEquals(1, fixture.outbox.pendingBatch(10).size());
        assertEquals(1, relay.relayBatch(10).published());
        assertEquals(0, relay.relayBatch(10).published());
        List<QueuedMessage> messages = queue.readNew("worker", 10);
        assertEquals(event, CoursePublishedEvent.fromFields(messages.getFirst().fields()));
        assertEquals(messages.getFirst().id().toString(), fixture.jdbc.queryForObject(
                "SELECT stream_id FROM publication_outbox", String.class));
        var result = consume(messages);
        assertEquals(1, result.applied());
        assertEquals(1, fixture.count("notifications"));
        assertEquals(0, queue.pendingCount());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
    }

    @Test
    void realRedisCommandFailureRetainsTaskForRepairAndRetry() {
        CoursePublishedEvent event = fixture.service.publish(1, "Retry");
        redis.del(stream);
        redis.set(stream, "wrong type");
        var failed = relay.relayBatch(10);
        assertEquals(0, failed.published());
        assertEquals(event.eventId(), failed.failures().getFirst().eventId());
        assertEquals(new PublicationTask(event, 1), fixture.outbox.pendingBatch(10).getFirst());
        redis.del(stream);
        queue.ensureGroup();
        assertEquals(1, relay.relayBatch(10).published());
        assertEquals(1, consume(queue.readNew("worker", 10)).applied());
        assertEquals(1, fixture.count("notifications"));
    }

    @Test
    void lostBrokerResponseReplaysSameEventWithDifferentStreamIds() {
        CoursePublishedEvent event = fixture.service.publish(1, "Lost response");
        CourseEventQueue uncertain = spy(queue);
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new JedisConnectionException("simulated lost response after real XADD");
        }).doCallRealMethod().when(uncertain).publish(event);
        PublicationRelay retrying = new PublicationRelay(fixture.outbox, uncertain);
        assertEquals(1, retrying.relayBatch(10).failures().size());
        assertEquals(1, redis.xlen(stream));
        assertEquals(1, retrying.relayBatch(10).published());
        assertDuplicateDeliveryHasOneEffect(event);
    }

    @Test
    void failedDatabaseMarkerAfterRealPublishAllowsSafeConsumerReplay() {
        CoursePublishedEvent event = fixture.service.publish(1, "Marker gap");
        PublicationOutbox failing = spy(fixture.outbox);
        doThrow(new DataAccessResourceFailureException("simulated marker failure")).when(failing).markPublished(any(), any());
        assertThrows(DataAccessResourceFailureException.class,
                () -> new PublicationRelay(failing, queue).relayBatch(10));
        assertEquals(1, redis.xlen(stream));
        assertEquals(new PublicationTask(event, 0), fixture.outbox.pendingBatch(10).getFirst());
        assertEquals(1, relay.relayBatch(10).published());
        assertDuplicateDeliveryHasOneEffect(event);
    }

    @Test
    void rolledBackBusinessAndEventNeverReachRedis() {
        PublicationOutbox failing = spy(fixture.outbox);
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new DataAccessResourceFailureException("simulated failure after outbox insert");
        }).when(failing).enqueue(any());
        var service = new CoursePublishingService(fixture.jdbc, failing, fixture.transactions);
        assertThrows(DataAccessResourceFailureException.class, () -> service.publish(1, "Rolled back"));
        assertEquals(0, fixture.count("courses"));
        assertEquals(0, fixture.count("outbox"));
        assertEquals(0, relay.relayBatch(10).published());
        assertEquals(0, redis.xlen(stream));
    }

    private NotificationConsumer.ProcessResult consume(List<QueuedMessage> messages) {
        NotificationInbox inbox = new NotificationInbox(fixture.jdbc, fixture.transactions, "notifications");
        return new NotificationConsumer(queue, inbox).processBatch(messages);
    }

    private void assertDuplicateDeliveryHasOneEffect(CoursePublishedEvent event) {
        List<QueuedMessage> messages = queue.readNew("worker", 10);
        assertEquals(2, messages.size());
        assertNotEquals(messages.getFirst().id(), messages.getLast().id());
        assertEquals(event, CoursePublishedEvent.fromFields(messages.getFirst().fields()));
        assertEquals(event, CoursePublishedEvent.fromFields(messages.getLast().fields()));
        var result = consume(messages);
        assertEquals(1, result.applied());
        assertEquals(1, result.duplicates());
        assertEquals(2, result.acknowledged());
        assertEquals(1, fixture.count("notifications"));
        assertEquals(0, queue.pendingCount());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
    }
}
