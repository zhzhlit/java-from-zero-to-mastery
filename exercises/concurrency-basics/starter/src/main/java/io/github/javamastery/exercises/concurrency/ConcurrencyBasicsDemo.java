package io.github.javamastery.exercises.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrencyBasicsDemo {
    public static void main(String[] args) throws Exception {
        CompletionCounter counter = new CompletionCounter();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try (executor) {
            var runner = new BatchTaskRunner(executor);
            List<Callable<String>> tasks = List.of(
                    () -> complete("Java", counter), () -> complete("Redis", counter), () -> complete("RPC", counter));
            System.out.println("Results in input order: " + runner.run(tasks, Duration.ofSeconds(3)));
            System.out.println("Completed tasks: " + counter.completed());
        }
        System.out.println("Executor terminated: " + executor.isTerminated());
    }

    private static String complete(String name, CompletionCounter counter) {
        counter.recordCompletion();
        return name;
    }
}
