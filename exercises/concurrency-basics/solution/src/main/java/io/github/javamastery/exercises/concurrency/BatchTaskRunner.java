package io.github.javamastery.exercises.concurrency;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BatchTaskRunner {
    private final ExecutorService executor;

    public BatchTaskRunner(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public <T> List<T> run(List<? extends Callable<T>> tasks, Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(tasks);
        Objects.requireNonNull(timeout);
        if (tasks.size() > 1000 || timeout.isNegative() || timeout.isZero()
                || timeout.compareTo(Duration.ofMinutes(1)) > 0) {
            throw new IllegalArgumentException("批次最多 1000 项，预算必须大于零且不超过 1 分钟");
        }
        var snapshot = List.copyOf(tasks);
        List<Future<T>> futures = executor.invokeAll(snapshot, timeout.toNanos(), TimeUnit.NANOSECONDS);
        // A cancelled member means the batch budget expired, even if another member failed earlier.
        if (futures.stream().anyMatch(Future::isCancelled)) {
            throw new TimeoutException("批次超时，未完成任务已请求取消");
        }
        List<T> results = new ArrayList<>(futures.size());
        for (Future<T> future : futures) {
            results.add(future.get());
        }
        // Callable may legitimately return null. The local list is never mutated after publication.
        return Collections.unmodifiableList(results);
    }
}
