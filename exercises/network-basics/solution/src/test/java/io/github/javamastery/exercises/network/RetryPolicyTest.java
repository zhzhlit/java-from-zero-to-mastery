package io.github.javamastery.exercises.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RetryPolicy")
class RetryPolicyTest {
    private final RetryPolicy policy = new RetryPolicy();

    @Test
    @DisplayName("幂等方法遇到临时失败可以重试")
    void retriesIdempotentMethodsForTemporaryFailures() {
        assertTrue(policy.shouldRetry("GET", 503));
        assertTrue(policy.shouldRetry("delete", 429));
    }

    @Test
    @DisplayName("默认不重试非幂等方法或永久失败")
    void doesNotRetryUnsafeOrPermanentFailures() {
        assertFalse(policy.shouldRetry("POST", 503));
        assertFalse(policy.shouldRetry("GET", 404));
        assertFalse(policy.shouldRetry(null, 503));
    }

    @Test
    @DisplayName("按尝试次数给出退避延迟并设置上限")
    void calculatesBackoffDelay() {
        assertEquals(100, policy.nextDelayMillis(1));
        assertEquals(400, policy.nextDelayMillis(3));
        assertEquals(1600, policy.nextDelayMillis(10));
    }

    @Test
    @DisplayName("尝试次数必须从 1 开始")
    void rejectsInvalidAttempt() {
        assertThrows(IllegalArgumentException.class, () -> policy.nextDelayMillis(0));
    }
}
