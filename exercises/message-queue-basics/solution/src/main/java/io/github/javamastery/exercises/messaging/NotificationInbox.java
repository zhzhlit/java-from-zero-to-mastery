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
        Objects.requireNonNull(event);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Inbox 必须独立完成数据库事务后才能确认消息");
        }
        try {
            transactions.executeWithoutResult(status -> {
                if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                    throw new IllegalStateException("Inbox 需要实际数据库事务");
                }
                jdbc.update("INSERT INTO inbox_receipts (consumer_group, event_id, course_id, title) VALUES (?, ?, ?, ?)",
                        group, event.eventId().toString(), event.courseId(), event.title());
                jdbc.update("INSERT INTO course_notifications (consumer_group, event_id, course_id, title) VALUES (?, ?, ?, ?)",
                        group, event.eventId().toString(), event.courseId(), event.title());
            });
            return true;
        } catch (DuplicateKeyException duplicate) {
            // Inspect committed state only after the failed transaction has rolled back.
            var committed = jdbc.query("SELECT course_id, title FROM inbox_receipts WHERE consumer_group = ? AND event_id = ?",
                    (rs, rowNum) -> new CoursePublishedEvent(event.eventId(), rs.getLong("course_id"), rs.getString("title")),
                    group, event.eventId().toString()).stream().findFirst();
            if (committed.isEmpty()) {
                throw duplicate;
            }
            if (!event.equals(committed.get())) {
                throw new EventPayloadConflictException(duplicate);
            }
            return false;
        }
    }

    String groupName() {
        return group;
    }
}
