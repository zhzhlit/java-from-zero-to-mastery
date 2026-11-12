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
        String key = catalog.key(id);
        return transactions.execute(status -> {
            Course updated = source.rename(id, title);
            outbox.enqueue(id, key);
            return updated;
        });
    }
}
