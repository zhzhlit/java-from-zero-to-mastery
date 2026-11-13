package io.github.javamastery.exercises.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class CompletionCounter {
    private final AtomicInteger completed = new AtomicInteger();

    public int recordCompletion() {
        return completed.incrementAndGet();
    }

    public int completed() {
        return completed.get();
    }
}
