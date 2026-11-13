package io.github.javamastery.exercises.concurrency;

import java.util.concurrent.ThreadPoolExecutor;

public final class BoundedTaskPools {
    private BoundedTaskPools() {
    }

    public static ThreadPoolExecutor newFixedPool(int workers, int capacity, OverflowPolicy policy) {
        throw new UnsupportedOperationException("练习：创建有界线程池，显式选择拒绝策略，关闭后不得静默丢弃任务");
    }

    public enum OverflowPolicy { ABORT, CALLER_RUNS }
}
