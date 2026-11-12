package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamGroupInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Disabled("练习：完成 CourseEventQueue 后移除 @Disabled")
class CourseEventQueueTest {
    private static final String STREAM = "learning:mq:test";
    private final JedisPooled redis = mock(JedisPooled.class);
    private final CourseEventQueue queue = new CourseEventQueue(redis, STREAM, "notifications");
    private final StreamEntryID id = new StreamEntryID("1000-1");
    private final CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), 1, "Java Messaging");

    @Test
    void publishesStructuredFieldsAndDecodesEvent() {
        when(redis.xadd(STREAM, StreamEntryID.NEW_ENTRY, event.toFields())).thenReturn(id);
        assertEquals(id, queue.publish(event));
        assertEquals(event, CoursePublishedEvent.fromFields(event.toFields()));
        verify(redis).xadd(STREAM, StreamEntryID.NEW_ENTRY, event.toFields());
    }

    @Test
    void onlyExistingGroupErrorIsTolerated() {
        doThrow(new JedisDataException("BUSYGROUP Consumer Group name already exists"))
                .when(redis).xgroupCreate(STREAM, "notifications", new StreamEntryID("0-0"), true);
        queue.ensureGroup();
        doThrow(new JedisDataException("NOPERM denied"))
                .when(redis).xgroupCreate(STREAM, "notifications", new StreamEntryID("0-0"), true);
        assertThrows(JedisDataException.class, queue::ensureGroup);
    }

    @Test
    void readsNewMessagesWithoutAutomaticallyAcknowledging() {
        when(redis.xreadGroup(eq("notifications"), eq("worker"), any(XReadGroupParams.class),
                eq(Map.of(STREAM, StreamEntryID.UNRECEIVED_ENTRY))))
                .thenReturn(List.of(Map.entry(STREAM, List.of(new StreamEntry(id, event.toFields())))));
        var messages = queue.readNew("worker", 5);
        assertEquals(id, messages.getFirst().id());
        assertEquals(event, CoursePublishedEvent.fromFields(messages.getFirst().fields()));
        verify(redis, never()).xack(anyString(), anyString(), any(StreamEntryID.class));
    }

    @Test
    void ownPendingReadUsesHistoryOffset() {
        assertTrue(queue.readOwnPending("worker", 5).isEmpty());
        verify(redis).xreadGroup(eq("notifications"), eq("worker"), any(XReadGroupParams.class),
                eq(Map.of(STREAM, new StreamEntryID("0-0"))));
    }

    @Test
    void missingReadResultIsEmptyButConnectionErrorsPropagate() {
        assertTrue(queue.readNew("worker", 5).isEmpty());
        when(redis.xreadGroup(eq("notifications"), eq("worker"), any(XReadGroupParams.class), any()))
                .thenThrow(new JedisConnectionException("redis unavailable"));
        assertThrows(JedisConnectionException.class, () -> queue.readNew("worker", 5));
    }

    @Test
    void receivedFieldsAreDefensivelyCopied() {
        Map<String, String> fields = new HashMap<>(event.toFields());
        QueuedMessage message = new QueuedMessage(id, fields);
        fields.put("title", "Changed");
        assertEquals(event.title(), message.fields().get("title"));
        assertThrows(UnsupportedOperationException.class, () -> message.fields().put("title", "Changed"));
    }

    @Test
    void acknowledgementAndPendingCountUseCorrectGroup() {
        when(redis.xack(STREAM, "notifications", id)).thenReturn(1L, 0L);
        when(redis.xinfoGroups(STREAM)).thenReturn(List.of(
                new StreamGroupInfo(Map.of("name", "other", "consumers", 1L, "pending", 9L, "last-delivered-id", id)),
                new StreamGroupInfo(Map.of("name", "notifications", "consumers", 1L, "pending", 1L, "last-delivered-id", id))));
        assertEquals(1, queue.pendingCount());
        assertTrue(queue.acknowledge(id));
        assertFalse(queue.acknowledge(id));
        when(redis.xinfoGroups(STREAM)).thenReturn(List.of());
        assertThrows(IllegalStateException.class, queue::pendingCount);
    }

    @Test
    void rejectsInvalidNamesAndBatchLimitsBeforeRedis() {
        assertThrows(IllegalArgumentException.class, () -> new CourseEventQueue(redis, "bad stream", "group"));
        assertThrows(IllegalArgumentException.class, () -> new CourseEventQueue(redis, STREAM, "bad group"));
        assertThrows(IllegalArgumentException.class, () -> queue.readNew("bad consumer", 1));
        assertThrows(IllegalArgumentException.class, () -> queue.readNew("worker", 0));
        assertThrows(IllegalArgumentException.class, () -> queue.readOwnPending("worker", 101));
        assertThrows(NullPointerException.class, () -> queue.acknowledge(null));
        verifyNoInteractions(redis);
    }

    @Test
    void rejectsInvalidPayloadWithoutAcknowledging() {
        Map<String, String> fields = new HashMap<>(event.toFields());
        fields.put("schemaVersion", "2");
        assertThrows(IllegalArgumentException.class, () -> CoursePublishedEvent.fromFields(fields));
        assertThrows(IllegalArgumentException.class, () -> CoursePublishedEvent.fromFields(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new CoursePublishedEvent(UUID.randomUUID(), 0, "Title"));
        assertThrows(IllegalArgumentException.class, () -> new CoursePublishedEvent(UUID.randomUUID(), 1, " "));
        assertThrows(IllegalArgumentException.class, () -> new CoursePublishedEvent(UUID.randomUUID(), 1, "a".repeat(201)));
        verifyNoInteractions(redis);
    }

    @Test
    void deletedPendingPayloadRemainsInvalidInsteadOfBeingAcknowledged() {
        QueuedMessage deleted = new QueuedMessage(id, null);
        assertEquals(id, deleted.id());
        assertThrows(IllegalArgumentException.class, () -> CoursePublishedEvent.fromFields(deleted.fields()));
        verifyNoInteractions(redis);
    }
}
