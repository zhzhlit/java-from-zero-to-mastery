package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.javamastery.exercises.messaging.StreamFailurePolicy.FailureKind;
import static io.github.javamastery.exercises.messaging.StreamFailurePolicy.Outcome;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResilientNotificationConsumerTest {
    private final CourseEventQueue queue = new CourseEventQueue(mock(JedisPooled.class), "learning:mq:test", "notifications");
    private final NotificationInbox inbox = mock(NotificationInbox.class);
    private final StreamFailurePolicy policy = mock(StreamFailurePolicy.class);
    private final StreamEntryID id = new StreamEntryID("1000-1");
    private final CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), 1, "Healthy");
    private ResilientNotificationConsumer consumer;

    @BeforeEach
    void configurePolicy() {
        when(inbox.groupName()).thenReturn("notifications");
        when(policy.groupName()).thenReturn("notifications");
        when(policy.streamName()).thenReturn(queue.streamName());
        when(policy.begin(anyString(), any())).thenReturn(decision(Outcome.READY));
        when(policy.acknowledgeSuccess(anyString(), any())).thenReturn(decision(Outcome.ACKNOWLEDGED));
        consumer = new ResilientNotificationConsumer(queue, inbox, policy);
        clearInvocations(inbox, policy);
    }

    @Test
    void malformedEventIsPermanentAndFailureCauseRemainsVisible() {
        when(policy.recordFailure("worker", id, FailureKind.INVALID_EVENT)).thenReturn(decision(Outcome.DEAD_LETTERED));
        var result = consumer.processBatch("worker", List.of(new QueuedMessage(id, Map.of())));
        assertEquals(1, result.deadLettered());
        assertEquals(IllegalArgumentException.class, result.failures().getFirst().cause().getClass());
        verifyNoInteractions(inbox);
    }

    @Test
    void transientFailureDoesNotBlockHealthyMessageInSameBatch() {
        CoursePublishedEvent healthy = new CoursePublishedEvent(UUID.randomUUID(), 2, "Second");
        StreamEntryID secondId = new StreamEntryID("1000-2");
        var failure = new TransientDataAccessResourceException("temporary database problem");
        when(inbox.storeIfNew(event)).thenThrow(failure);
        when(inbox.storeIfNew(healthy)).thenReturn(true);
        when(policy.recordFailure("worker", id, FailureKind.TRANSIENT_DATABASE)).thenReturn(decision(Outcome.RETRY));
        var result = consumer.processBatch("worker", List.of(new QueuedMessage(id, event.toFields()),
                new QueuedMessage(secondId, healthy.toFields())));
        assertEquals(1, result.retryPending());
        assertEquals(1, result.applied());
        assertSame(failure, result.failures().getFirst().cause());
        verify(policy).acknowledgeSuccess("worker", secondId);
        verify(policy, never()).acknowledgeSuccess("worker", id);
    }

    @Test
    void payloadConflictAndBusinessConstraintHaveDistinctPermanentCodes() {
        when(inbox.storeIfNew(event)).thenThrow(new EventPayloadConflictException(new IllegalStateException("conflict")));
        when(policy.recordFailure("worker", id, FailureKind.EVENT_ID_CONFLICT)).thenReturn(decision(Outcome.DEAD_LETTERED));
        assertEquals(1, consumer.processBatch("worker", List.of(new QueuedMessage(id, event.toFields()))).deadLettered());
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("constraint")).when(inbox).storeIfNew(event);
        when(policy.recordFailure("worker", id, FailureKind.BUSINESS_CONSTRAINT)).thenReturn(decision(Outcome.DEAD_LETTERED));
        assertEquals(1, consumer.processBatch("worker", List.of(new QueuedMessage(id, event.toFields()))).deadLettered());
    }

    @Test
    void configurationAndAcknowledgementFailuresAreNotPoisonFailures() {
        when(inbox.storeIfNew(event)).thenThrow(new IllegalStateException("configuration problem"));
        assertThrows(IllegalStateException.class, () -> consumer.processBatch("worker", List.of(new QueuedMessage(id, event.toFields()))));
        org.mockito.Mockito.doReturn(true).when(inbox).storeIfNew(event);
        when(policy.acknowledgeSuccess("worker", id)).thenThrow(new JedisConnectionException("ack unavailable"));
        assertThrows(JedisConnectionException.class, () -> consumer.processBatch("worker", List.of(new QueuedMessage(id, event.toFields()))));
        verify(policy, never()).recordFailure(anyString(), any(), any());
    }

    @Test
    void alreadyTerminalMessageDoesNotDecodeOrRunBusinessAgain() {
        when(policy.begin("worker", id)).thenReturn(decision(Outcome.ALREADY_DEAD_LETTERED));
        assertEquals(1, consumer.processBatch("worker", List.of(new QueuedMessage(id, Map.of()))).alreadyDeadLettered());
        verifyNoInteractions(inbox);
        verify(policy, never()).acknowledgeSuccess(anyString(), any());
    }

    @Test
    void successAndDuplicatePathsRemainDistinctAndInputsAreBounded() {
        when(inbox.storeIfNew(event)).thenReturn(true, false);
        QueuedMessage message = new QueuedMessage(id, event.toFields());
        assertEquals(1, consumer.processBatch("worker", List.of(message)).applied());
        assertEquals(1, consumer.processBatch("worker", List.of(message)).duplicates());
        assertThrows(IllegalArgumentException.class, () -> consumer.processBatch("bad worker", List.of(message)));
        assertThrows(IllegalArgumentException.class, () -> consumer.processBatch("worker", Collections.nCopies(101, message)));
        when(policy.streamName()).thenReturn("other-stream");
        assertThrows(IllegalArgumentException.class, () -> new ResilientNotificationConsumer(queue, inbox, policy));
    }

    private StreamFailurePolicy.Decision decision(Outcome outcome) {
        return new StreamFailurePolicy.Decision(outcome, 0, "");
    }
}
