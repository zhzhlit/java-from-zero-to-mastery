package io.github.javamastery.exercises.messaging;

import java.util.List;
import java.util.Objects;

public class NotificationConsumer {
    private final CourseEventQueue queue;
    private final NotificationInbox inbox;

    public NotificationConsumer(CourseEventQueue queue, NotificationInbox inbox) {
        this.queue = Objects.requireNonNull(queue);
        this.inbox = Objects.requireNonNull(inbox);
        if (!queue.groupName().equals(inbox.groupName())) {
            throw new IllegalArgumentException("队列与 Inbox 的消费组必须一致");
        }
    }

    public ProcessResult processBatch(List<QueuedMessage> messages) {
        Objects.requireNonNull(messages);
        if (messages.size() > 100) {
            throw new IllegalArgumentException("处理批次不能超过 100");
        }
        int applied = 0;
        int duplicates = 0;
        for (QueuedMessage message : messages) {
            CoursePublishedEvent event = CoursePublishedEvent.fromFields(message.fields());
            if (inbox.storeIfNew(event)) {
                applied++;
            } else {
                duplicates++;
            }
            if (!queue.acknowledge(message.id())) {
                throw new IllegalStateException("业务已提交，但消息未被确认");
            }
        }
        return new ProcessResult(applied, duplicates, messages.size());
    }

    public record ProcessResult(int applied, int duplicates, int acknowledged) {
    }
}
