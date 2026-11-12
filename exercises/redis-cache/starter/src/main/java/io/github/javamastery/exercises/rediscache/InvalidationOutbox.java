package io.github.javamastery.exercises.rediscache;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

public class InvalidationOutbox {
    private final JdbcTemplate jdbc;

    public InvalidationOutbox(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void enqueue(long courseId, String cacheKey) {
        throw new UnsupportedOperationException("练习：检查实际事务和参数，插入待处理任务");
    }

    public List<InvalidationTask> pendingBatch(int limit) {
        throw new UnsupportedOperationException("练习：限制批次为 1 到 1000，优先读取较少尝试的待处理任务");
    }

    public void markDelivered(long id) {
        throw new UnsupportedOperationException("练习：确认单条待处理任务已完成，更新计数并清除错误码");
    }

    public void markFailed(long id) {
        throw new UnsupportedOperationException("练习：保留待处理状态，记录失败计数与固定错误码");
    }

    private void requireSingleUpdate(int rows) {
        if (rows != 1) {
            throw new IllegalStateException("失效任务不存在或已被其他处理器改变");
        }
    }
}
