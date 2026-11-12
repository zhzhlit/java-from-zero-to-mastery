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
        throw new UnsupportedOperationException("练习：在实际业务事务内保存事件 ID、目标和载荷快照");
    }

    public List<PublicationTask> pendingBatch(int limit) {
        throw new UnsupportedOperationException("练习：按目标读取有界待发布任务");
    }

    public void markPublished(UUID eventId, StreamEntryID streamId) {
        throw new UnsupportedOperationException("练习：记录 Redis 返回的 Stream ID 并标记已发布");
    }

    public void markFailed(UUID eventId) {
        throw new UnsupportedOperationException("练习：记录发布失败，保留待投递任务");
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
