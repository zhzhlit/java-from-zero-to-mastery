package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseEventQueueIT {
    private final String stream = "learning:mq:it:" + UUID.randomUUID();
    private JedisPooled producer;
    private JedisPooled consumer;
    private CourseEventQueue publisher;
    private CourseEventQueue queue;

    @BeforeEach
    void connect() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        producer = new JedisPooled(host, port);
        consumer = new JedisPooled(host, port);
        publisher = new CourseEventQueue(producer, stream, "notifications");
        queue = new CourseEventQueue(consumer, stream, "notifications");
    }

    @AfterEach
    void cleanOnlyOwnStream() {
        try {
            if (producer != null) {
                producer.del(stream);
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            if (producer != null) {
                producer.close();
            }
        }
    }

    @Test
    void consumesBacklogAndAcknowledgesWithoutDeletingEntry() {
        CoursePublishedEvent event = event(1);
        StreamEntryID id = publisher.publish(event);
        queue.ensureGroup();
        QueuedMessage received = queue.readNew("worker", 1).getFirst();
        assertEquals(id, received.id());
        assertEquals(event, CoursePublishedEvent.fromFields(received.fields()));
        assertEquals(1, queue.pendingCount());
        assertTrue(queue.acknowledge(id));
        assertEquals(0, queue.pendingCount());
        assertEquals(1, producer.xlen(stream));
        assertFalse(queue.acknowledge(id));
    }

    @Test
    void sameGroupDistributesNewEntriesAcrossConsumers() {
        queue.ensureGroup();
        publisher.publish(event(1));
        publisher.publish(event(2));
        var first = queue.readNew("worker-1", 1).getFirst();
        var second = queue.readNew("worker-2", 1).getFirst();
        assertNotEquals(first.id(), second.id());
        assertTrue(queue.readNew("worker-1", 1).isEmpty());
        assertEquals(2, queue.pendingCount());
    }

    @Test
    void ownPendingCanBeReplayedAfterConsumerObjectRecreation() {
        queue.ensureGroup();
        StreamEntryID id = publisher.publish(event(1));
        queue.readNew("worker-1", 1);
        CourseEventQueue restarted = new CourseEventQueue(consumer, stream, "notifications");
        assertTrue(restarted.readOwnPending("worker-2", 10).isEmpty());
        assertEquals(id, restarted.readOwnPending("worker-1", 10).getFirst().id());
        assertEquals(id, restarted.readOwnPending("worker-1", 10).getFirst().id());
        assertTrue(restarted.acknowledge(id));
        assertTrue(restarted.readOwnPending("worker-1", 10).isEmpty());
    }

    @Test
    void independentGroupsEachReceiveSameEvent() {
        queue.ensureGroup();
        CourseEventQueue analytics = new CourseEventQueue(consumer, stream, "analytics");
        analytics.ensureGroup();
        StreamEntryID id = publisher.publish(event(1));
        assertEquals(id, queue.readNew("worker", 1).getFirst().id());
        assertEquals(id, analytics.readNew("worker", 1).getFirst().id());
        queue.acknowledge(id);
        assertEquals(0, queue.pendingCount());
        assertEquals(1, analytics.pendingCount());
    }

    @Test
    void malformedEventRemainsPending() {
        queue.ensureGroup();
        var fields = new HashMap<>(event(1).toFields());
        fields.put("schemaVersion", "unsupported");
        StreamEntryID id = producer.xadd(stream, StreamEntryID.NEW_ENTRY, fields);
        QueuedMessage message = queue.readNew("worker", 1).getFirst();
        assertThrows(IllegalArgumentException.class, () -> CoursePublishedEvent.fromFields(message.fields()));
        assertEquals(1, queue.pendingCount());
        assertEquals(id, queue.readOwnPending("worker", 10).getFirst().id());
    }

    @Test
    void boundsEachNonBlockingRead() {
        queue.ensureGroup();
        for (int i = 1; i <= 3; i++) {
            publisher.publish(event(i));
        }
        assertEquals(2, queue.readNew("worker", 2).size());
        assertEquals(1, queue.readNew("worker", 2).size());
        assertEquals(3, queue.pendingCount());
    }

    @Test
    void deletedPendingEntryKeepsIdButCannotBeDecoded() {
        queue.ensureGroup();
        StreamEntryID id = publisher.publish(event(1));
        queue.readNew("worker", 1);
        producer.xdel(stream, id);
        QueuedMessage deleted = queue.readOwnPending("worker", 1).getFirst();
        assertEquals(id, deleted.id());
        assertThrows(IllegalArgumentException.class, () -> CoursePublishedEvent.fromFields(deleted.fields()));
        assertEquals(1, queue.pendingCount());
    }

    @Test
    void repeatedGroupInitializationDoesNotResetDeliveredPosition() {
        queue.ensureGroup();
        StreamEntryID id = publisher.publish(event(1));
        queue.readNew("worker", 1);
        queue.acknowledge(id);
        queue.ensureGroup();
        assertTrue(queue.readNew("worker", 1).isEmpty());
        assertEquals(0, queue.pendingCount());
    }

    private CoursePublishedEvent event(long id) {
        return new CoursePublishedEvent(UUID.randomUUID(), id, "Course " + id);
    }
}
