package io.github.javamastery.exercises.messaging;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.UUID;

public class CoursePublishingService {
    private final JdbcTemplate jdbc;
    private final PublicationOutbox outbox;
    private final TransactionTemplate transactions;

    public CoursePublishingService(JdbcTemplate jdbc, PublicationOutbox outbox, TransactionTemplate transactions) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.outbox = Objects.requireNonNull(outbox);
        this.transactions = Objects.requireNonNull(transactions);
        if (jdbc.getDataSource() != outbox.dataSource()) {
            throw new IllegalArgumentException("业务和 Outbox 必须使用同一数据源");
        }
    }

    public CoursePublishedEvent publish(long courseId, String title) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("发布服务必须独立完成事务，不能嵌套调用");
        }
        CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), courseId, title);
        transactions.executeWithoutResult(status -> {
            // Check the bound resource before either write, including a misconfigured transaction manager.
            outbox.requireTransaction();
            jdbc.update("INSERT INTO published_courses (id, title) VALUES (?, ?)", event.courseId(), event.title());
            outbox.enqueue(event);
        });
        return event;
    }
}
