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
        throw new UnsupportedOperationException("练习：解码后提交 Inbox 与业务写入，再确认消息并汇总结果");
    }

    public record ProcessResult(int applied, int duplicates, int acknowledged) {
    }
}
