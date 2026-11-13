package io.github.javamastery.exercises.rediscache;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

public class TransactionalCourseService {
    private final CourseRepository source;
    private final AfterCommitInvalidator invalidator;
    private final TransactionTemplate transactions;

    public TransactionalCourseService(CourseRepository source, AfterCommitInvalidator invalidator,
                                      TransactionTemplate transactions) {
        this.source = Objects.requireNonNull(source);
        this.invalidator = Objects.requireNonNull(invalidator);
        this.transactions = Objects.requireNonNull(transactions);
    }

    public Course rename(long id, String title) {
        throw new UnsupportedOperationException("练习：使用 TransactionTemplate 更新数据库并注册提交后失效");
    }
}
