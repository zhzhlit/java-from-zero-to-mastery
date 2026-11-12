package io.github.javamastery.exercises.rediscache;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OutboxDispatcher {
    private final JedisPooled redis;
    private final InvalidationOutbox outbox;

    public OutboxDispatcher(JedisPooled redis, InvalidationOutbox outbox) {
        this.redis = Objects.requireNonNull(redis);
        this.outbox = Objects.requireNonNull(outbox);
    }

    public synchronized DispatchResult dispatchBatch(int limit) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("任务处理器必须在业务事务之外运行");
        }
        int delivered = 0;
        List<DispatchFailure> failures = new ArrayList<>();
        for (InvalidationTask task : outbox.pendingBatch(limit)) {
            try {
                redis.del(task.cacheKey());
            } catch (JedisException exception) {
                outbox.markFailed(task.id());
                failures.add(new DispatchFailure(task.id(), exception));
                continue;
            }
            // A failed acknowledgement leaves the task pending for an idempotent replay.
            outbox.markDelivered(task.id());
            delivered++;
        }
        return new DispatchResult(delivered, failures);
    }

    public record DispatchFailure(long taskId, JedisException cause) {
    }

    public record DispatchResult(int delivered, List<DispatchFailure> failures) {
        public DispatchResult {
            failures = List.copyOf(failures);
        }
    }
}
