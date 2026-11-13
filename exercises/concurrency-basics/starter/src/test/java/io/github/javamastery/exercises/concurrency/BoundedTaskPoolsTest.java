package io.github.javamastery.exercises.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.javamastery.exercises.concurrency.BoundedTaskPools.OverflowPolicy.ABORT;
import static io.github.javamastery.exercises.concurrency.BoundedTaskPools.OverflowPolicy.CALLER_RUNS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Disabled("练习：完成有界线程池与拒绝策略后移除 @Disabled")
class BoundedTaskPoolsTest {
    @Test
    void validatesBoundsAndCreatesNamedNonDaemonWorkers() {
        for (int workers : new int[]{0, 33}) {
            assertThrows(IllegalArgumentException.class, () -> BoundedTaskPools.newFixedPool(workers, 1, ABORT));
        }
        for (int capacity : new int[]{0, 1001}) {
            assertThrows(IllegalArgumentException.class, () -> BoundedTaskPools.newFixedPool(1, capacity, ABORT));
        }
        assertThrows(NullPointerException.class, () -> BoundedTaskPools.newFixedPool(1, 1, null));
        try (var pool = BoundedTaskPools.newFixedPool(2, 3, ABORT)) {
            assertEquals(2, pool.getCorePoolSize());
            assertEquals(2, pool.getMaximumPoolSize());
            assertEquals(3, pool.getQueue().remainingCapacity());
            Thread thread = pool.getThreadFactory().newThread(() -> { });
            assertTrue(thread.getName().startsWith("bounded-worker-"));
            assertFalse(thread.isDaemon());
        }
    }

    @Test
    void abortRejectsOverflowWithoutDroppingAlreadyAcceptedTasks() throws Exception {
        try (var full = new FullPool(ABORT)) {
            assertEquals(1, full.pool.getQueue().size());
            assertThrows(RejectedExecutionException.class, () -> full.pool.submit(() -> "overflow"));
            full.gate.countDown();
            assertEquals("running", full.running.get(2, TimeUnit.SECONDS));
            assertEquals("queued", full.queued.get(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void callerRunsUsesSubmittingThreadWithoutEnlargingWorkerPoolOrQueue() throws Exception {
        try (var full = new FullPool(CALLER_RUNS)) {
            var future = full.pool.submit(Thread::currentThread);
            assertTrue(future.isDone());
            assertSame(Thread.currentThread(), future.get(2, TimeUnit.SECONDS));
            assertEquals(1, full.pool.getPoolSize());
            assertEquals(1, full.pool.getQueue().size());
        }
    }

    @Test
    void callerRunsPreservesSubmitFailureAndDirectExecuteFailure() throws Exception {
        var failure = new IllegalStateException("inline failure");
        try (var full = new FullPool(CALLER_RUNS)) {
            Future<String> future = full.pool.submit(() -> { throw failure; });
            assertSame(failure, assertThrows(ExecutionException.class, () -> future.get(2, TimeUnit.SECONDS)).getCause());
            assertSame(failure, assertThrows(IllegalStateException.class,
                    () -> full.pool.execute(() -> { throw failure; })));
        }
    }

    @Test
    void bothPoliciesRejectObservedShutdownInsteadOfReturningUnfinishedFuture() {
        for (var policy : BoundedTaskPools.OverflowPolicy.values()) {
            try (var pool = BoundedTaskPools.newFixedPool(1, 1, policy)) {
                pool.shutdown();
                assertThrows(RejectedExecutionException.class, () -> pool.submit(() -> "never"));
                assertThrows(RejectedExecutionException.class, () -> pool.execute(() -> { }));
            }
        }
    }

    @Test
    void cancelledQueuedFutureKeepsItsSlotUntilPurgedOrDequeued() throws Exception {
        try (var full = new FullPool(ABORT)) {
            assertTrue(full.queued.cancel(false));
            assertEquals(1, full.pool.getQueue().size());
            assertThrows(RejectedExecutionException.class, () -> full.pool.submit(() -> "still full"));
            full.pool.purge();
            assertEquals(0, full.pool.getQueue().size());
            var replacement = full.pool.submit(() -> "replacement");
            full.gate.countDown();
            assertEquals("replacement", replacement.get(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void shutdownNowReturnsQueuedWorkWhoseFutureStillNeedsExplicitCancellation() throws Exception {
        try (var full = new FullPool(ABORT)) {
            var neverStarted = full.pool.shutdownNow();
            assertEquals(1, neverStarted.size());
            assertSame(full.queued, neverStarted.getFirst());
            assertFalse(full.queued.isDone());
            for (Runnable task : neverStarted) {
                assertInstanceOf(Future.class, task).cancel(false);
            }
            assertTrue(full.queued.isCancelled());
            var interrupted = assertThrows(ExecutionException.class, () -> full.running.get(2, TimeUnit.SECONDS));
            assertInstanceOf(InterruptedException.class, interrupted.getCause());
            assertTrue(full.pool.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void gracefulShutdownDrainsAcceptedQueueButRejectsNewWork() throws Exception {
        try (var full = new FullPool(ABORT)) {
            full.pool.shutdown();
            assertFalse(full.pool.isTerminated());
            assertThrows(RejectedExecutionException.class, () -> full.pool.submit(() -> "new"));
            full.gate.countDown();
            assertEquals("running", full.running.get(2, TimeUnit.SECONDS));
            assertEquals("queued", full.queued.get(2, TimeUnit.SECONDS));
            assertTrue(full.pool.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void partiallyRejectedBatchCancelsItsQueuedFutureWithoutStoppingUnrelatedWork() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch gate = new CountDownLatch(1);
        AtomicInteger ran = new AtomicInteger();
        try (var pool = BoundedTaskPools.newFixedPool(1, 1, ABORT)) {
            var unrelated = pool.submit(() -> {
                started.countDown();
                gate.await();
                return "unrelated";
            });
            try {
                assertTrue(started.await(2, TimeUnit.SECONDS));
                List<Callable<Integer>> tasks = List.of(ran::incrementAndGet, ran::incrementAndGet);
                assertThrows(RejectedExecutionException.class,
                        () -> new BatchTaskRunner(pool).run(tasks, Duration.ofSeconds(1)));
                assertEquals(0, ran.get());
                assertTrue(assertInstanceOf(Future.class, pool.getQueue().peek()).isCancelled());
                assertFalse(unrelated.isDone());
            } finally {
                gate.countDown();
            }
        }
    }

    @Test
    void callerRunsCanBlockSubmissionBeyondBatchWaitBudget() throws Exception {
        CountDownLatch inlineStarted = new CountDownLatch(1);
        CountDownLatch inlineGate = new CountDownLatch(1);
        AtomicReference<List<String>> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        try (var full = new FullPool(CALLER_RUNS)) {
            Thread caller = Thread.ofPlatform().name("caller-runs-budget-test").start(() -> {
                try {
                    result.set(new BatchTaskRunner(full.pool).run(List.<Callable<String>>of(() -> {
                        inlineStarted.countDown();
                        inlineGate.await();
                        return "inline";
                    }), Duration.ofMillis(50)));
                } catch (Throwable problem) {
                    failure.set(problem);
                }
            });
            try {
                assertTrue(inlineStarted.await(2, TimeUnit.SECONDS));
                caller.join(150);
                assertTrue(caller.isAlive());
            } finally {
                inlineGate.countDown();
                caller.join(2000);
                if (caller.isAlive()) {
                    caller.interrupt();
                    caller.join(2000);
                }
            }
            assertFalse(caller.isAlive());
            // The observable guarantee is late return, not a specific deadline race after inline completion.
            if (failure.get() != null) {
                assertInstanceOf(TimeoutException.class, failure.get());
            } else {
                assertEquals(List.of("inline"), result.get());
            }
        }
    }

    private static final class FullPool implements AutoCloseable {
        final ThreadPoolExecutor pool;
        final CountDownLatch gate = new CountDownLatch(1);
        final Future<String> running;
        final Future<String> queued;

        FullPool(BoundedTaskPools.OverflowPolicy policy) throws Exception {
            pool = BoundedTaskPools.newFixedPool(1, 1, policy);
            CountDownLatch started = new CountDownLatch(1);
            try {
                running = pool.submit(() -> {
                    started.countDown();
                    gate.await();
                    return "running";
                });
                if (!started.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("worker did not start");
                queued = pool.submit(() -> "queued");
            } catch (Exception | Error failure) {
                gate.countDown();
                pool.close();
                throw failure;
            }
        }

        @Override
        public void close() {
            gate.countDown();
            pool.close();
        }
    }
}
