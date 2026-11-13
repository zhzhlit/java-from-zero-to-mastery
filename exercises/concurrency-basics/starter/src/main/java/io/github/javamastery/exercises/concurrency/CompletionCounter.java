package io.github.javamastery.exercises.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class CompletionCounter {
    private final AtomicInteger completed = new AtomicInteger();

    public int recordCompletion() {
        throw new UnsupportedOperationException("练习：原子增加完成计数并返回新值");
    }

    public int completed() {
        return completed.get();
    }
}
