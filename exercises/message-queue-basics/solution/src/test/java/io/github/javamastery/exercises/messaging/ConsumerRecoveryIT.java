package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumerRecoveryIT {
    private final String stream = "learning:mq:recovery-it:" + UUID.randomUUID();
    private JedisPooled originalClient;
    private JedisPooled replacementClient;
    private CourseEventQueue original;
    private CourseEventQueue replacement;
    private InboxFixture fixture;
    private final CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), 1, "Recovery");

    @BeforeEach
    void connect() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        originalClient = new JedisPooled(host, port);
        replacementClient = new JedisPooled(host, port);
        original = new CourseEventQueue(originalClient, stream, "notifications");
        replacement = new CourseEventQueue(replacementClient, stream, "notifications");
        fixture = new InboxFixture();
        original.ensureGroup();
    }

    @AfterEach
    void cleanOwnResources() {
        try {
            if (replacementClient != null) {
                replacementClient.del(stream);
            }
        } finally {
            try {
                if (fixture != null) {
                    fixture.close();
                }
            } finally {
                if (replacementClient != null) {
                    replacementClient.close();
                }
                if (originalClient != null) {
                    originalClient.close();
                }
            }
        }
    }

    @Test
    void claimsAnotherConsumerPendingAndProcessesIt() {
        StreamEntryID id = original.publish(event);
        original.readNew("original", 1);
        ClaimBatch claimed = replacement.claimIdle("replacement", Duration.ZERO, new StreamEntryID("0-0"), 10);
        assertEquals(id, claimed.messages().getFirst().id());
        assertTrue(original.readOwnPending("original", 10).isEmpty());
        assertEquals(id, replacement.readOwnPending("replacement", 10).getFirst().id());
        var result = new NotificationConsumer(replacement, fixture.inbox).processBatch(claimed.messages());
        assertEquals(new NotificationConsumer.ProcessResult(1, 0, 1), result);
        assertEquals(1, fixture.notifications());
        assertEquals(0, replacement.pendingCount());
    }

    @Test
    void acknowledgementFailureAfterCommitIsDeduplicatedDuringRecovery() {
        StreamEntryID id = original.publish(event);
        original.readNew("original", 1);
        assertTrue(fixture.inbox.storeIfNew(event));
        originalClient.close();
        assertThrows(JedisException.class, () -> original.acknowledge(id));
        ClaimBatch claimed = replacement.claimIdle("replacement", Duration.ZERO, new StreamEntryID("0-0"), 10);
        var result = new NotificationConsumer(replacement, fixture.inbox).processBatch(claimed.messages());
        assertEquals(new NotificationConsumer.ProcessResult(0, 1, 1), result);
        assertEquals(1, fixture.notifications());
        assertEquals(0, replacement.pendingCount());
    }

    @Test
    void idleThresholdDoesNotStealRecentlyDeliveredMessage() {
        original.publish(event);
        original.readNew("original", 1);
        assertTrue(replacement.claimIdle("replacement", Duration.ofHours(1), new StreamEntryID("0-0"), 10)
                .messages().isEmpty());
        assertEquals(1, original.readOwnPending("original", 10).size());
        assertTrue(replacement.readOwnPending("replacement", 10).isEmpty());
    }

    @Test
    void malformedClaimedMessageRemainsPendingWithoutBusinessWrite() {
        originalClient.xadd(stream, StreamEntryID.NEW_ENTRY, Map.of("schemaVersion", "unsupported"));
        original.readNew("original", 1);
        ClaimBatch claimed = replacement.claimIdle("replacement", Duration.ZERO, new StreamEntryID("0-0"), 10);
        assertThrows(IllegalArgumentException.class,
                () -> new NotificationConsumer(replacement, fixture.inbox).processBatch(claimed.messages()));
        assertEquals(0, fixture.notifications());
        assertEquals(0, fixture.receipts());
        assertEquals(1, replacement.pendingCount());
        assertEquals(1, replacement.readOwnPending("replacement", 10).size());
    }
}
