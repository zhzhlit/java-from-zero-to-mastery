package io.github.javamastery.exercises.concurrency;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class LockStateDemo {
    public static void main(String[] args) throws Exception {
        CourseSeatLedger ledger = new CourseSeatLedger(5);
        List<Callable<CourseSeatLedger.Reservation>> tasks = new ArrayList<>();
        for (int id = 0; id < 20; id++) {
            String learner = "learner-" + id;
            tasks.add(() -> ledger.reserve(learner));
        }
        try (var executor = Executors.newFixedThreadPool(4)) {
            var results = new BatchTaskRunner(executor).run(tasks, Duration.ofSeconds(3));
            System.out.println("Reserved: " + results.stream().filter(r -> r == CourseSeatLedger.Reservation.RESERVED).count());
            System.out.println("Full: " + results.stream().filter(r -> r == CourseSeatLedger.Reservation.FULL).count());
        }
        String from = ledger.snapshot().learners().stream().sorted().findFirst().orElseThrow();
        if (!ledger.transfer(from, "replacement")) throw new IllegalStateException("名额转让失败");
        System.out.println("After transfer reserved: " + ledger.snapshot().reserved());
        if (!ledger.cancel("replacement")) throw new IllegalStateException("名额取消失败");
        ledger.resize(7);
        System.out.println("After cancellation and resize available: " + ledger.snapshot().available());
    }
}
