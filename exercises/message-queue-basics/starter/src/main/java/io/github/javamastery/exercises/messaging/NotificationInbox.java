package io.github.javamastery.exercises.messaging;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

public class NotificationInbox {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final String group;

    public NotificationInbox(JdbcTemplate jdbc, TransactionTemplate transactions, String group) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.transactions = Objects.requireNonNull(transactions);
        if (group == null || !group.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("消费组名称不合法");
        }
        this.group = group;
    }

    public boolean storeIfNew(CoursePublishedEvent event) {
        throw new UnsupportedOperationException("练习：独立事务写入去重记录与通知，重复键回滚后检查已提交载荷");
    }

    String groupName() {
        return group;
    }
}
