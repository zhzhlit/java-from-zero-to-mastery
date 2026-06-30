package io.github.javamastery.exercises.network;

public class RetryPolicy {
    public boolean shouldRetry(String method, int statusCode) {
        return false;
    }

    public int nextDelayMillis(int attempt) {
        return 0;
    }
}
