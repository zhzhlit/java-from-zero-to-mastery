package io.github.javamastery.exercises.messaging;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.XReadGroupParams;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CourseEventQueue {
    private static final Pattern SAFE_STREAM = Pattern.compile("[A-Za-z0-9:_-]{1,160}");
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private final JedisPooled redis;
    private final String stream;
    private final String group;

    public CourseEventQueue(JedisPooled redis, String stream, String group) {
        this.redis = Objects.requireNonNull(redis);
        if (stream == null || !SAFE_STREAM.matcher(stream).matches()
                || group == null || !SAFE_NAME.matcher(group).matches()) {
            throw new IllegalArgumentException("Stream 或消费组名称不合法");
        }
        this.stream = stream;
        this.group = group;
    }

    public void ensureGroup() {
        throw new UnsupportedOperationException("练习：创建从 0-0 开始的消费组，只忽略 BUSYGROUP");
    }

    public StreamEntryID publish(CoursePublishedEvent event) {
        throw new UnsupportedOperationException("练习：用 XADD 发布结构化事件并返回服务器分配的 ID");
    }

    public List<QueuedMessage> readNew(String consumer, int limit) {
        throw new UnsupportedOperationException("练习：使用 > 读取本组未投递的新消息，不自动确认");
    }

    public List<QueuedMessage> readOwnPending(String consumer, int limit) {
        throw new UnsupportedOperationException("练习：使用 0-0 读取该消费者自己的 pending");
    }

    public boolean acknowledge(StreamEntryID id) {
        throw new UnsupportedOperationException("练习：显式 XACK，区分确认成功与已不在 pending");
    }

    public long pendingCount() {
        throw new UnsupportedOperationException("练习：从 XINFO GROUPS 中读取指定消费组的 pending 总数");
    }

    private List<QueuedMessage> read(String consumer, int limit, StreamEntryID offset) {
        if (consumer == null || !SAFE_NAME.matcher(consumer).matches() || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("消费者名称不合法，或批次大小不在 1 到 100 之间");
        }
        var result = redis.xreadGroup(group, consumer, XReadGroupParams.xReadGroupParams().count(limit),
                Map.of(stream, offset));
        if (result == null) {
            return List.of();
        }
        return result.stream().flatMap(entry -> entry.getValue().stream())
                .map(entry -> new QueuedMessage(entry.getID(), entry.getFields())).toList();
    }
}
