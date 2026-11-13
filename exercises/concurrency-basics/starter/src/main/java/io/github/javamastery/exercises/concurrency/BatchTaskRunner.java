package io.github.javamastery.exercises.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class BatchTaskRunner {
    private final ExecutorService executor;

    public BatchTaskRunner(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public <T> List<T> run(List<? extends Callable<T>> tasks, Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("练习：校验输入，使用统一批次预算执行任务，并保留顺序与失败语义");
    }
}
