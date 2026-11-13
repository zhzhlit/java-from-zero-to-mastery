package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.messaging.CourseEventQueue;
import io.github.javamastery.exercises.messaging.NotificationInbox;
import io.github.javamastery.exercises.messaging.QueuedMessage;
import io.github.javamastery.exercises.search.CourseSearchIndex;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CoursePublicationWorkflow {
    private final ReviewCourseCatalog source;
    private final CourseSearchIndex index;
    private final NotificationInbox inbox;
    private final CourseEventQueue queue;

    public CoursePublicationWorkflow(ReviewCourseCatalog source, CourseSearchIndex index, JedisPooled redis, String group) {
        this.source = Objects.requireNonNull(source);
        this.index = Objects.requireNonNull(index);
        this.queue = new CourseEventQueue(redis, source.streamName(), group);
        this.inbox = new NotificationInbox(source.jdbc(), source.transactions(), group);
    }

    public BatchResult processBatch(List<QueuedMessage> messages) throws IOException {
        throw new UnsupportedOperationException("练习：校验来源、提交并刷新索引、写入幂等通知，再确认消息");
    }

    public int backfill() throws IOException {
        throw new UnsupportedOperationException("练习：从权威数据库补齐新索引，不依赖消费回执");
    }

    public CourseEventQueue queue() {
        return queue;
    }

    public record BatchResult(int acknowledged, int newNotifications, int duplicates) {
    }
}
