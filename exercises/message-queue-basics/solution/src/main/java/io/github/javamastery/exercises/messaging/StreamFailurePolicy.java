package io.github.javamastery.exercises.messaging;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class StreamFailurePolicy {
    private static final String SCRIPT = loadScript();
    private final JedisPooled redis;
    private final String stream;
    private final String group;
    private final int maximumFailures;

    public StreamFailurePolicy(JedisPooled redis, CourseEventQueue queue, int maximumFailures) {
        this.redis = Objects.requireNonNull(redis);
        Objects.requireNonNull(queue);
        if (maximumFailures < 1 || maximumFailures > 100) {
            throw new IllegalArgumentException("失败次数上限必须为 1 到 100");
        }
        this.stream = queue.streamName();
        this.group = queue.groupName();
        this.maximumFailures = maximumFailures;
    }

    public Decision begin(String consumer, StreamEntryID id) {
        return execute("BEGIN", consumer, id, maximumFailures, "NONE");
    }

    public Decision acknowledgeSuccess(String consumer, StreamEntryID id) {
        return execute("SUCCESS", consumer, id, maximumFailures, "NONE");
    }

    public Decision recordFailure(String consumer, StreamEntryID id, FailureKind kind) {
        Objects.requireNonNull(kind);
        return execute("FAILURE", consumer, id, kind.retryable ? maximumFailures : 1, kind.name());
    }

    public String failureStateKey() {
        return stream + ":retry:" + group;
    }

    public String deadLetterStream() {
        return stream + ":dead:" + group;
    }

    String streamName() {
        return stream;
    }

    String groupName() {
        return group;
    }

    private Decision execute(String operation, String consumer, StreamEntryID id, int limit, String errorCode) {
        Objects.requireNonNull(id);
        if (consumer == null || !consumer.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("消费者名称不合法");
        }
        Object response = redis.eval(SCRIPT, List.of(stream, failureStateKey(), deadLetterStream()),
                List.of(operation, group, consumer, id.toString(), Integer.toString(limit), errorCode));
        if (!(response instanceof List<?> values) || values.size() != 3
                || !(values.get(0) instanceof String outcome) || !(values.get(1) instanceof Number failures)
                || !(values.get(2) instanceof String deadId)) {
            throw new IllegalStateException("Redis 处理结果格式不合法");
        }
        return new Decision(Outcome.valueOf(outcome), Math.toIntExact(failures.longValue()), deadId);
    }

    private static String loadScript() {
        try (var input = StreamFailurePolicy.class.getResourceAsStream("/stream-processing.lua")) {
            if (input == null) {
                throw new IllegalStateException("缺少消息处理脚本");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public enum Outcome { READY, ACKNOWLEDGED, RETRY, DEAD_LETTERED, ALREADY_DEAD_LETTERED }

    public enum FailureKind {
        INVALID_EVENT(false), EVENT_ID_CONFLICT(false), BUSINESS_CONSTRAINT(false), TRANSIENT_DATABASE(true);

        private final boolean retryable;

        FailureKind(boolean retryable) {
            this.retryable = retryable;
        }
    }

    public record Decision(Outcome outcome, int failures, String deadLetterId) {
    }
}
