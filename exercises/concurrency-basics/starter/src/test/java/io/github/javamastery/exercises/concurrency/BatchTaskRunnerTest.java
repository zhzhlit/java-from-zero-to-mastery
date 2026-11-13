package io.github.javamastery.exercises.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Disabled("练习：完成批次任务和共享计数后移除 @Disabled")
class BatchTaskRunnerTest {
    @Test
    void tasksCanOverlapButResultsKeepInputOrder() throws Exception {
        CountDownLatch secondFinished = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Callable<String>> tasks = List.of(() -> {
                if (!secondFinished.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("second task never ran");
                return "first";
            }, () -> {
                secondFinished.countDown();
                return "second";
            });
            assertEquals(List.of("first", "second"), new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(3)));
        }
    }

    @Test
    void emptyBatchReturnsEmptyAndDoesNotCloseCallerExecutor() throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            assertTrue(new BatchTaskRunner(executor).run(List.<Callable<String>>of(), Duration.ofSeconds(1)).isEmpty());
            assertFalse(executor.isShutdown());
            assertEquals("next", executor.submit(() -> "next").get(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void nullTaskResultIsAllowedButResultListCannotBeChanged() throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var result = new BatchTaskRunner(executor).run(List.<Callable<String>>of(() -> null), Duration.ofSeconds(1));
            assertEquals(Arrays.asList((String) null), result);
            assertThrows(UnsupportedOperationException.class, () -> result.add("change"));
        }
    }

    @Test
    void fixedPoolLimitsActiveWorkersWhileTasksComplete() throws Exception {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        CountDownLatch firstTwo = new CountDownLatch(2);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tasks.add(() -> {
                int now = active.incrementAndGet();
                peak.accumulateAndGet(now, Math::max);
                try {
                    firstTwo.countDown();
                    if (!firstTwo.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("workers did not overlap");
                    return now;
                } finally {
                    active.decrementAndGet();
                }
            });
        }
        try (var executor = Executors.newFixedThreadPool(2)) {
            assertEquals(8, new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(3)).size());
            assertEquals(2, peak.get());
            assertEquals(0, active.get());
        }
    }

    @Test
    void executionFailurePreservesCauseAndDoesNotPretendToBeFailFast() throws Exception {
        var failure = new IllegalStateException("task failed");
        AtomicInteger anotherTask = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Callable<Integer>> tasks = List.of(() -> { throw failure; }, anotherTask::incrementAndGet);
            var thrown = assertThrows(ExecutionException.class,
                    () -> new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(2)));
            assertSame(failure, thrown.getCause());
            assertEquals(1, anotherTask.get());
        }
    }

    @Test
    void cancellationExceptionThrownByTaskIsStillExecutionFailure() throws Exception {
        var failure = new CancellationException("task chose to fail");
        try (var executor = Executors.newSingleThreadExecutor()) {
            var thrown = assertThrows(ExecutionException.class, () -> new BatchTaskRunner(executor)
                    .run(List.<Callable<String>>of(() -> { throw failure; }), Duration.ofSeconds(1)));
            assertSame(failure, thrown.getCause());
        }
    }

    @Test
    void timeoutRequestsInterruptionOfUnfinishedTask() throws Exception {
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            Callable<String> task = () -> {
                try {
                    gate.await();
                    return "released";
                } finally {
                    stopped.countDown();
                }
            };
            try {
                assertThrows(TimeoutException.class,
                        () -> new BatchTaskRunner(executor).run(List.of(task), Duration.ofSeconds(1)));
                assertTrue(stopped.await(2, TimeUnit.SECONDS));
            } finally {
                gate.countDown();
            }
        }
    }

    @Test
    void batchTimeoutTakesPriorityOverAnEarlierTaskFailure() throws Exception {
        CountDownLatch gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Callable<String>> tasks = List.of(() -> { throw new IllegalStateException("failed first"); }, () -> {
                gate.await();
                return "late";
            });
            try {
                assertThrows(TimeoutException.class, () -> new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(1)));
            } finally {
                gate.countDown();
            }
        }
    }

    @Test
    void interruptingCallerPropagatesAndCancelsItsUnfinishedBatch() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        AtomicReference<Throwable> observed = new AtomicReference<>();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var runner = new BatchTaskRunner(executor);
            Callable<String> task = () -> {
                started.countDown();
                try {
                    gate.await();
                    return "released";
                } finally {
                    stopped.countDown();
                }
            };
            Thread caller = Thread.ofPlatform().name("batch-caller-test").start(() -> {
                try {
                    runner.run(List.of(task), Duration.ofSeconds(10));
                } catch (Throwable failure) {
                    observed.set(failure);
                }
            });
            try {
                assertTrue(started.await(2, TimeUnit.SECONDS));
                caller.interrupt();
                caller.join(2000);
                assertFalse(caller.isAlive());
                assertInstanceOf(InterruptedException.class, observed.get());
                assertTrue(stopped.await(2, TimeUnit.SECONDS));
            } finally {
                gate.countDown();
                caller.interrupt();
                caller.join(2000);
            }
        }
    }

    @Test
    void cancellationCannotForceTaskThatTemporarilyIgnoresInterruptionToStop() throws Exception {
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            Callable<String> task = () -> {
                started.countDown();
                try {
                    while (gate.getCount() != 0) {
                        try {
                            gate.await();
                        } catch (InterruptedException ignoredForThisTest) {
                            // Deliberate bad task: the test always releases its gate in finally.
                        }
                    }
                    return "released";
                } finally {
                    stopped.countDown();
                }
            };
            try {
                assertThrows(TimeoutException.class,
                        () -> new BatchTaskRunner(executor).run(List.of(task), Duration.ofSeconds(1)));
                assertEquals(0, started.getCount());
                assertEquals(1, stopped.getCount());
            } finally {
                gate.countDown();
                assertTrue(stopped.await(2, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void invalidArgumentsDoNotSubmitWork() {
        AtomicInteger ran = new AtomicInteger();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var runner = new BatchTaskRunner(executor);
            List<Callable<Integer>> tasks = List.of(ran::incrementAndGet);
            assertThrows(NullPointerException.class, () -> new BatchTaskRunner(null));
            assertThrows(NullPointerException.class, () -> runner.run(null, Duration.ofSeconds(1)));
            assertThrows(NullPointerException.class, () -> runner.run(tasks, null));
            assertThrows(NullPointerException.class, () -> runner.run(Arrays.asList((Callable<Integer>) null), Duration.ofSeconds(1)));
            assertThrows(IllegalArgumentException.class, () -> runner.run(Collections.nCopies(1001, tasks.getFirst()), Duration.ofSeconds(1)));
            for (Duration timeout : List.of(Duration.ZERO, Duration.ofSeconds(-1), Duration.ofSeconds(61),
                    Duration.ofSeconds(Long.MAX_VALUE))) {
                assertThrows(IllegalArgumentException.class, () -> runner.run(tasks, timeout));
            }
            assertEquals(0, ran.get());
        }
    }

    @Test
    void shutdownExecutorRejectionIsNotConvertedToEmptySuccess() {
        var executor = Executors.newSingleThreadExecutor();
        executor.shutdown();
        assertThrows(RejectedExecutionException.class,
                () -> new BatchTaskRunner(executor).run(List.<Callable<String>>of(() -> "never"), Duration.ofSeconds(1)));
    }

    @Test
    void atomicCounterDoesNotLoseIncrementsFromOverlappingTasks() throws Exception {
        CompletionCounter counter = new CompletionCounter();
        CountDownLatch ready = new CountDownLatch(4);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tasks.add(() -> {
                ready.countDown();
                if (!ready.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("workers did not overlap");
                for (int n = 0; n < 1000; n++) counter.recordCompletion();
                return counter.completed();
            });
        }
        try (var executor = Executors.newFixedThreadPool(4)) {
            new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(3));
            assertEquals(4000, counter.completed());
        }
    }
}
