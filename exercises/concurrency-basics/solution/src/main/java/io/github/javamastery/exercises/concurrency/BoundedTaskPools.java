package io.github.javamastery.exercises.concurrency;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class BoundedTaskPools {
    private BoundedTaskPools() {
    }

    public static ThreadPoolExecutor newFixedPool(int workers, int capacity, OverflowPolicy policy) {
        Objects.requireNonNull(policy);
        if (workers < 1 || workers > 32 || capacity < 1 || capacity > 1000) {
            throw new IllegalArgumentException("工作线程为 1 到 32，队列容量为 1 到 1000");
        }
        RejectedExecutionHandler handler = switch (policy) {
            case ABORT -> new ThreadPoolExecutor.AbortPolicy();
            case CALLER_RUNS -> (task, executor) -> {
                if (executor.isShutdown()) {
                    throw new RejectedExecutionException("线程池已关闭");
                }
                // Unlike the JDK CallerRunsPolicy, an observed shutdown must not silently discard a FutureTask.
                task.run();
            };
        };
        return new ThreadPoolExecutor(workers, workers, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacity), Thread.ofPlatform().name("bounded-worker-", 0).daemon(false).factory(), handler);
    }

    public enum OverflowPolicy { ABORT, CALLER_RUNS }
}
