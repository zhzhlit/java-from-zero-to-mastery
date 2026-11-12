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
        throw new UnsupportedOperationException("练习：业务事务外处理有界批次，先 DEL 再确认，返回失败明细");
    }

    public record DispatchFailure(long taskId, JedisException cause) {
    }

    public record DispatchResult(int delivered, List<DispatchFailure> failures) {
        public DispatchResult {
            failures = List.copyOf(failures);
        }
    }
}
