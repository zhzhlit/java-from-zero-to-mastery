package io.github.javamastery.exercises.messaging;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

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
        throw new UnsupportedOperationException("练习：业务课程与固定事件在同一事务提交，不访问 Redis");
    }
}
