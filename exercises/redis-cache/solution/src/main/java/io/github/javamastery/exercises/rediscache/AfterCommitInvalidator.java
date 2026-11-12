package io.github.javamastery.exercises.rediscache;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

public class AfterCommitInvalidator {
    private final RedisCourseCatalog catalog;

    public AfterCommitInvalidator(RedisCourseCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    public void invalidateAfterCommit(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须大于 0");
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("提交后失效必须在实际事务中注册");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                catalog.invalidate(id);
            }
        });
    }
}
