package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.messaging.CourseEventQueue;
import io.github.javamastery.exercises.messaging.CoursePublishedEvent;
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
        Objects.requireNonNull(messages);
        ReviewCourseCatalog.requireOutsideTransaction();
        if (messages.size() > 100) {
            throw new IllegalArgumentException("消息批次不能超过 100");
        }
        int notifications = 0;
        int duplicates = 0;
        for (QueuedMessage message : messages) {
            CoursePublishedEvent event = CoursePublishedEvent.fromFields(message.fields());
            var course = source.findDocument(event.courseId())
                    .orElseThrow(() -> new IllegalStateException("发布事件对应的权威课程不存在"));
            if (!course.title().equals(event.title())) {
                throw new IllegalStateException("发布事件与权威课程载荷不一致");
            }
            // Never skip the index based on a database receipt: these stores do not share a transaction.
            index.upsert(course);
            index.commit();
            index.refresh();
            if (inbox.storeIfNew(event)) {
                notifications++;
            } else {
                duplicates++;
            }
            if (!queue.acknowledge(message.id())) {
                throw new IllegalStateException("消息未处于该消费组的 pending 状态");
            }
        }
        return new BatchResult(messages.size(), notifications, duplicates);
    }

    public int backfill() throws IOException {
        ReviewCourseCatalog.requireOutsideTransaction();
        var courses = source.allDocuments();
        for (var course : courses) {
            index.upsert(course);
        }
        index.commit();
        index.refresh();
        return courses.size();
    }

    public CourseEventQueue queue() {
        return queue;
    }

    public record BatchResult(int acknowledged, int newNotifications, int duplicates) {
    }
}
