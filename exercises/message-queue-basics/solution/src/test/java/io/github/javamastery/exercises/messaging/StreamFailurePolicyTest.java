package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StreamFailurePolicyTest {
    private final JedisPooled redis = mock(JedisPooled.class);
    private final CourseEventQueue queue = new CourseEventQueue(redis, "learning:mq:test", "notifications");
    private final StreamFailurePolicy policy = new StreamFailurePolicy(redis, queue, 3);
    private final StreamEntryID id = new StreamEntryID("1000-1");

    @Test
    void beginUsesExplicitKeysAndOwnershipArguments() {
        List<String> keys = List.of(queue.streamName(), policy.failureStateKey(), policy.deadLetterStream());
        List<String> args = List.of("BEGIN", "notifications", "worker", id.toString(), "3", "NONE");
        when(redis.eval(anyString(), eq(keys), eq(args))).thenReturn(List.of("READY", 0L, ""));
        assertEquals(StreamFailurePolicy.Outcome.READY, policy.begin("worker", id).outcome());
        verify(redis).eval(anyString(), eq(keys), eq(args));
    }

    @Test
    void permanentFailuresUseOneAttemptWhileTransientFailuresUseConfiguredLimit() {
        for (var kind : StreamFailurePolicy.FailureKind.values()) {
            String limit = kind == StreamFailurePolicy.FailureKind.TRANSIENT_DATABASE ? "3" : "1";
            List<String> args = List.of("FAILURE", "notifications", "worker", id.toString(), limit, kind.name());
            when(redis.eval(anyString(), anyList(), eq(args))).thenReturn(List.of("DEAD_LETTERED", 1L, "2000-1"));
            assertEquals("2000-1", policy.recordFailure("worker", id, kind).deadLetterId());
            verify(redis).eval(anyString(), anyList(), eq(args));
        }
    }

    @Test
    void invalidInputsFailBeforeRedis() {
        assertThrows(IllegalArgumentException.class, () -> new StreamFailurePolicy(redis, queue, 0));
        assertThrows(IllegalArgumentException.class, () -> new StreamFailurePolicy(redis, queue, 101));
        assertThrows(IllegalArgumentException.class, () -> policy.begin("bad worker", id));
        assertThrows(NullPointerException.class, () -> policy.begin("worker", null));
        verifyNoInteractions(redis);
    }

    @Test
    void malformedRepliesAndConnectionErrorsAreNotConvertedToSuccess() {
        when(redis.eval(anyString(), anyList(), anyList())).thenReturn(List.of("READY"));
        assertThrows(IllegalStateException.class, () -> policy.begin("worker", id));
        when(redis.eval(anyString(), anyList(), anyList())).thenThrow(new JedisConnectionException("redis unavailable"));
        assertThrows(JedisConnectionException.class, () -> policy.begin("worker", id));
    }

    @Test
    void successUsesItsOwnAcknowledgementOperation() {
        List<String> args = List.of("SUCCESS", "notifications", "worker", id.toString(), "3", "NONE");
        when(redis.eval(anyString(), anyList(), eq(args))).thenReturn(List.of("ACKNOWLEDGED", 0L, ""));
        assertEquals(StreamFailurePolicy.Outcome.ACKNOWLEDGED, policy.acknowledgeSuccess("worker", id).outcome());
        verify(redis).eval(anyString(), anyList(), eq(args));
    }

    @Test
    void terminalReplyKeepsExistingDeadLetterIdentity() {
        when(redis.eval(anyString(), anyList(), anyList())).thenReturn(List.of("ALREADY_DEAD_LETTERED", 3L, "2000-1"));
        assertEquals(new StreamFailurePolicy.Decision(StreamFailurePolicy.Outcome.ALREADY_DEAD_LETTERED, 3, "2000-1"),
                policy.begin("worker", id));
    }
}
