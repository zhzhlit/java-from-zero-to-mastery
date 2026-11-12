package io.github.javamastery.exercises.rediscache;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

public class OutboxCourseService {
    private final CourseRepository source;
    private final InvalidationOutbox outbox;
    private final RedisCourseCatalog catalog;
    private final TransactionTemplate transactions;

    public OutboxCourseService(CourseRepository source, InvalidationOutbox outbox,
                              RedisCourseCatalog catalog, TransactionTemplate transactions) {
        this.source = Objects.requireNonNull(source);
        this.outbox = Objects.requireNonNull(outbox);
        this.catalog = Objects.requireNonNull(catalog);
        this.transactions = Objects.requireNonNull(transactions);
    }

    public Course rename(long id, String title) {
        throw new UnsupportedOperationException("练习：在同一事务中更新课程并记录实际失效键，不调用 Redis");
    }
}
