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
    private static final StreamEntryID HISTORY_START = new StreamEntryID("0-0");
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
        try {
            redis.xgroupCreate(stream, group, HISTORY_START, true);
        } catch (JedisDataException exception) {
            if (exception.getMessage() == null || !exception.getMessage().startsWith("BUSYGROUP ")) {
                throw exception;
            }
        }
    }

    public StreamEntryID publish(CoursePublishedEvent event) {
        return redis.xadd(stream, StreamEntryID.NEW_ENTRY, Objects.requireNonNull(event).toFields());
    }

    public List<QueuedMessage> readNew(String consumer, int limit) {
        return read(consumer, limit, StreamEntryID.UNRECEIVED_ENTRY);
    }

    public List<QueuedMessage> readOwnPending(String consumer, int limit) {
        return read(consumer, limit, HISTORY_START);
    }

    public boolean acknowledge(StreamEntryID id) {
        return redis.xack(stream, group, Objects.requireNonNull(id)) == 1;
    }

    public long pendingCount() {
        return redis.xinfoGroups(stream).stream().filter(info -> group.equals(info.getName()))
                .findFirst().orElseThrow(() -> new IllegalStateException("消费组不存在")).getPending();
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
