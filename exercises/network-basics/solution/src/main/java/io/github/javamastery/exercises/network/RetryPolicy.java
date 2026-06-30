package io.github.javamastery.exercises.network;

import java.util.Locale;
import java.util.Set;

public class RetryPolicy {
    private static final Set<String> IDEMPOTENT_METHODS = Set.of(
            "GET", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE"
    );
    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(
            408, 429, 500, 502, 503, 504
    );

    public boolean shouldRetry(String method, int statusCode) {
        if (method == null || method.isBlank()) {
            return false;
        }
        String normalizedMethod = method.trim().toUpperCase(Locale.ROOT);
        return IDEMPOTENT_METHODS.contains(normalizedMethod)
                && RETRYABLE_STATUS_CODES.contains(statusCode);
    }

    public int nextDelayMillis(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must start at 1");
        }
        int cappedAttempt = Math.min(attempt, 5);
        return 100 * (1 << (cappedAttempt - 1));
    }
}
