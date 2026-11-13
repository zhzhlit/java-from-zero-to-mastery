package io.github.javamastery.exercises.rediscache;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

public class InvalidationOutbox {
    private final JdbcTemplate jdbc;

    public InvalidationOutbox(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void enqueue(long courseId, String cacheKey) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("失效任务必须与业务更新在实际事务中写入");
        }
        if (courseId <= 0 || cacheKey == null || cacheKey.isBlank() || cacheKey.length() > 160) {
            throw new IllegalArgumentException("任务课程或缓存键不合法");
        }
        jdbc.update("INSERT INTO cache_invalidations (course_id, cache_key) VALUES (?, ?)", courseId, cacheKey);
    }

    public List<InvalidationTask> pendingBatch(int limit) {
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("批次大小必须为 1 到 1000");
        }
        return jdbc.query("SELECT id, course_id, cache_key, attempts, error_code FROM cache_invalidations "
                        + "WHERE delivered = FALSE ORDER BY attempts, id FETCH FIRST ? ROWS ONLY",
                (rs, rowNum) -> new InvalidationTask(rs.getLong("id"), rs.getLong("course_id"),
                        rs.getString("cache_key"), rs.getInt("attempts"), rs.getString("error_code")), limit);
    }

    public void markDelivered(long id) {
        requireSingleUpdate(jdbc.update("UPDATE cache_invalidations SET delivered = TRUE, "
                + "attempts = attempts + 1, error_code = NULL WHERE id = ? AND delivered = FALSE", id));
    }

    public void markFailed(long id) {
        requireSingleUpdate(jdbc.update("UPDATE cache_invalidations SET attempts = attempts + 1, "
                + "error_code = 'REDIS_DELETE_FAILED' WHERE id = ? AND delivered = FALSE", id));
    }

    private void requireSingleUpdate(int rows) {
        if (rows != 1) {
            throw new IllegalStateException("失效任务不存在或已被其他处理器改变");
        }
    }
}
