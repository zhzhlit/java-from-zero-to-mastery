package io.github.javamastery.exercises.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ThreadPoolQueueDemo {
    public static void main(String[] args) throws Exception {
        for (var policy : BoundedTaskPools.OverflowPolicy.values()) {
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch gate = new CountDownLatch(1);
            try (var pool = BoundedTaskPools.newFixedPool(1, 1, policy)) {
                var running = pool.submit(() -> {
                    started.countDown();
                    gate.await();
                    return "running";
                });
                try {
                    if (!started.await(2, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("工作线程未启动");
                    }
                    var queued = pool.submit(() -> "queued");
                    System.out.println(policy + " queued: " + pool.getQueue().size());
                    if (policy == BoundedTaskPools.OverflowPolicy.ABORT) {
                        boolean rejected = false;
                        try {
                            pool.submit(() -> "overflow");
                        } catch (RejectedExecutionException expected) {
                            rejected = true;
                        }
                        if (!rejected) throw new IllegalStateException("饱和时没有拒绝任务");
                        System.out.println("ABORT overflow rejected: true");
                    } else {
                        var inline = pool.submit(Thread::currentThread);
                        System.out.println("CALLER_RUNS executes on caller: " + (inline.get() == Thread.currentThread()));
                    }
                    gate.countDown();
                    running.get(2, TimeUnit.SECONDS);
                    queued.get(2, TimeUnit.SECONDS);
                } finally {
                    gate.countDown();
                }
            }
        }
    }
}
