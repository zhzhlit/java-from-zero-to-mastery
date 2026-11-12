package io.github.javamastery.exercises.messaging;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import redis.clients.jedis.StreamEntryID;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PublicationOutbox {
    private final JdbcTemplate jdbc;
    private final String stream;

    public PublicationOutbox(JdbcTemplate jdbc, String stream) {
        this.jdbc = Objects.requireNonNull(jdbc);
        Objects.requireNonNull(jdbc.getDataSource());
        if (stream == null || !stream.matches("[A-Za-z0-9:_-]{1,160}")) {
            throw new IllegalArgumentException("Stream 名称不合法");
        }
        this.stream = stream;
    }

    public void enqueue(CoursePublishedEvent event) {
        requireTransaction();
        Objects.requireNonNull(event);
        jdbc.update("INSERT INTO publication_outbox (event_id, destination, course_id, title) VALUES (?, ?, ?, ?)",
                event.eventId().toString(), stream, event.courseId(), event.title());
    }

    public List<PublicationTask> pendingBatch(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("批次大小必须为 1 到 100");
        }
        return jdbc.query("SELECT event_id, course_id, title, attempts FROM publication_outbox "
                        + "WHERE destination = ? AND published = FALSE ORDER BY attempts, sequence_no FETCH FIRST ? ROWS ONLY",
                (rs, rowNum) -> new PublicationTask(new CoursePublishedEvent(UUID.fromString(rs.getString("event_id")),
                        rs.getLong("course_id"), rs.getString("title")), rs.getInt("attempts")), stream, limit);
    }

    public void markPublished(UUID eventId, StreamEntryID streamId) {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(streamId);
        requireSingleUpdate(jdbc.update("UPDATE publication_outbox SET published = TRUE, stream_id = ?, "
                        + "attempts = attempts + 1, error_code = NULL WHERE event_id = ? AND destination = ? AND published = FALSE",
                streamId.toString(), eventId.toString(), stream));
    }

    public void markFailed(UUID eventId) {
        Objects.requireNonNull(eventId);
        requireSingleUpdate(jdbc.update("UPDATE publication_outbox SET attempts = attempts + 1, "
                        + "error_code = 'REDIS_PUBLISH_FAILED' WHERE event_id = ? AND destination = ? AND published = FALSE",
                eventId.toString(), stream));
    }

    void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.hasResource(dataSource())) {
            throw new IllegalStateException("业务和发布事件必须使用同一数据源的实际事务");
        }
    }

    DataSource dataSource() {
        return jdbc.getDataSource();
    }

    String streamName() {
        return stream;
    }

    private void requireSingleUpdate(int rows) {
        if (rows != 1) {
            throw new IllegalStateException("发布任务不存在或已被其他处理器改变");
        }
    }
}
