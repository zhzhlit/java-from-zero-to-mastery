package io.github.javamastery.exercises.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.github.javamastery.exercises.concurrency.CourseSeatLedger.Reservation.ALREADY_RESERVED;
import static io.github.javamastery.exercises.concurrency.CourseSeatLedger.Reservation.FULL;
import static io.github.javamastery.exercises.concurrency.CourseSeatLedger.Reservation.RESERVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Disabled("练习：完成名额账本的复合状态保护后移除 @Disabled")
class CourseSeatLedgerTest {
    @Test
    void overlappingReservationsCannotOversellCapacity() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(10);
        CountDownLatch ready = new CountDownLatch(8);
        List<Callable<CourseSeatLedger.Reservation>> tasks = new ArrayList<>();
        for (int id = 0; id < 100; id++) {
            String learner = "learner-" + id;
            tasks.add(() -> {
                meet(ready);
                return ledger.reserve(learner);
            });
        }
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(4));
            assertEquals(10, results.stream().filter(r -> r == RESERVED).count());
            assertEquals(90, results.stream().filter(r -> r == FULL).count());
            assertEquals(10, ledger.snapshot().reserved());
            assertEquals(0, ledger.snapshot().available());
        }
    }

    @Test
    void concurrentDuplicateLearnerConsumesExactlyOneSeat() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(8);
        CountDownLatch ready = new CountDownLatch(4);
        List<Callable<CourseSeatLedger.Reservation>> tasks = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            tasks.add(() -> {
                meet(ready);
                return ledger.reserve("same-learner");
            });
        }
        try (var executor = Executors.newFixedThreadPool(4)) {
            var results = new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(3));
            assertEquals(1, results.stream().filter(r -> r == RESERVED).count());
            assertEquals(31, results.stream().filter(r -> r == ALREADY_RESERVED).count());
            assertEquals(new CourseSeatLedger.Snapshot(8, 7, Set.of("same-learner")), ledger.snapshot());
        }
    }

    @Test
    void cancellationIsIdempotentAndOnlyReleasesOneSeat() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(2);
        ledger.reserve("first");
        ledger.reserve("second");
        assertTrue(ledger.cancel("first"));
        assertFalse(ledger.cancel("first"));
        assertEquals(1, ledger.snapshot().available());
        assertEquals(RESERVED, ledger.reserve("third"));
        assertEquals(Set.of("second", "third"), ledger.snapshot().learners());
        assertEquals(FULL, ledger.reserve("fourth"));
        assertEquals(ALREADY_RESERVED, ledger.reserve("second"));
    }

    @Test
    void transferChangesMembershipWithoutChangingSeatCountAndFailuresDoNotPartiallyModify() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(3);
        ledger.reserve("first");
        ledger.reserve("second");
        assertTrue(ledger.transfer("first", "third"));
        var before = ledger.snapshot();
        assertEquals(Set.of("second", "third"), before.learners());
        assertEquals(1, before.available());
        assertFalse(ledger.transfer("missing", "fourth"));
        assertFalse(ledger.transfer("third", "second"));
        assertThrows(IllegalArgumentException.class, () -> ledger.transfer("third", "third"));
        assertEquals(before, ledger.snapshot());
    }

    @Test
    void resizingChecksExistingReservationsBeforeChangingBothFields() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(3);
        ledger.reserve("first");
        ledger.reserve("second");
        var before = ledger.snapshot();
        assertThrows(IllegalStateException.class, () -> ledger.resize(1));
        assertEquals(before, ledger.snapshot());
        ledger.resize(4);
        assertEquals(2, ledger.snapshot().available());
        ledger.reserve("third");
        ledger.resize(3);
        assertEquals(new CourseSeatLedger.Snapshot(3, 0, Set.of("first", "second", "third")), ledger.snapshot());
    }

    @Test
    void snapshotsAreImmutableCoherentAndDetachedFromLaterUpdates() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(2);
        ledger.reserve("first");
        var snapshot = ledger.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.learners().add("tamper"));
        ledger.cancel("first");
        ledger.reserve("second");
        assertEquals(Set.of("first"), snapshot.learners());
        assertEquals(Set.of("second"), ledger.snapshot().learners());
        assertThrows(IllegalArgumentException.class, () -> new CourseSeatLedger.Snapshot(2, 2, Set.of("invalid")));
    }

    @Test
    void preInterruptedCallerCannotMutateLedger() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(3);
        ledger.reserve("first");
        var before = ledger.snapshot();
        List<Callable<Boolean>> attempts = List.of(() -> { ledger.reserve("second"); return true; },
                () -> ledger.cancel("first"), () -> ledger.transfer("first", "second"),
                () -> { ledger.resize(4); return true; });
        try (var executor = Executors.newSingleThreadExecutor()) {
            for (Callable<Boolean> attempt : attempts) {
                assertTrue(executor.submit(() -> {
                    Thread.currentThread().interrupt();
                    assertThrows(InterruptedException.class, attempt::call);
                    assertFalse(Thread.currentThread().isInterrupted());
                    return true;
                }).get(2, TimeUnit.SECONDS));
            }
            assertEquals(before, ledger.snapshot());
            assertEquals(RESERVED, executor.submit(() -> ledger.reserve("second")).get(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void invalidInputsAndExceptionalMutationDoNotLeaveLockHeld() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new CourseSeatLedger(0));
        assertThrows(IllegalArgumentException.class, () -> new CourseSeatLedger(10001));
        CourseSeatLedger ledger = new CourseSeatLedger(2);
        for (String learner : new String[]{null, " ", "bad learner", "x".repeat(65)}) {
            assertThrows(IllegalArgumentException.class, () -> ledger.reserve(learner));
            assertThrows(IllegalArgumentException.class, () -> ledger.cancel(learner));
            assertThrows(IllegalArgumentException.class, () -> ledger.transfer("valid", learner));
        }
        ledger.reserve("first");
        ledger.reserve("second");
        assertThrows(IllegalArgumentException.class, () -> ledger.resize(0));
        assertThrows(IllegalStateException.class, () -> ledger.resize(1));
        try (var executor = Executors.newSingleThreadExecutor()) {
            var operation = executor.submit(() -> ledger.cancel("first"));
            try {
                assertTrue(operation.get(2, TimeUnit.SECONDS));
            } finally {
                operation.cancel(true);
            }
        }
        assertEquals(1, ledger.snapshot().available());
    }

    @Test
    void snapshotsRemainCoherentDuringRepeatedMutations() throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(4);
        CountDownLatch ready = new CountDownLatch(4);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String learner = "worker-" + i;
            tasks.add(() -> {
                meet(ready);
                for (int n = 0; n < 100; n++) {
                    assertEquals(RESERVED, ledger.reserve(learner));
                    assertTrue(ledger.cancel(learner));
                }
                return 100;
            });
        }
        tasks.add(() -> {
            meet(ready);
            for (int n = 0; n < 200; n++) {
                var snapshot = ledger.snapshot();
                assertEquals(snapshot.capacity(), snapshot.available() + snapshot.reserved());
                assertTrue(snapshot.available() >= 0);
            }
            return 200;
        });
        try (var executor = Executors.newFixedThreadPool(4)) {
            new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(4));
        }
        assertEquals(new CourseSeatLedger.Snapshot(4, 4, Set.of()), ledger.snapshot());
    }

    @Test
    void differentLedgersHaveIndependentState() throws Exception {
        CourseSeatLedger first = new CourseSeatLedger(1);
        CourseSeatLedger second = new CourseSeatLedger(2);
        first.reserve("same");
        second.reserve("same");
        first.cancel("same");
        assertEquals(1, first.snapshot().available());
        assertEquals(Set.of("same"), second.snapshot().learners());
    }

    private void meet(CountDownLatch ready) throws InterruptedException {
        ready.countDown();
        if (!ready.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("workers did not reach start gate");
    }
}
