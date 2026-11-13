package io.github.javamastery.exercises.messaging;

import java.util.List;
import java.util.Objects;

import static io.github.javamastery.exercises.messaging.StreamFailurePolicy.FailureKind;
import static io.github.javamastery.exercises.messaging.StreamFailurePolicy.Outcome;

public class ResilientNotificationConsumer {
    private final NotificationInbox inbox;
    private final StreamFailurePolicy policy;

    public ResilientNotificationConsumer(CourseEventQueue queue, NotificationInbox inbox, StreamFailurePolicy policy) {
        Objects.requireNonNull(queue);
        this.inbox = Objects.requireNonNull(inbox);
        this.policy = Objects.requireNonNull(policy);
        if (!queue.groupName().equals(inbox.groupName()) || !queue.groupName().equals(policy.groupName())
                || !queue.streamName().equals(policy.streamName())) {
            throw new IllegalArgumentException("队列、Inbox 与失败策略的来源必须一致");
        }
    }

    public BatchResult processBatch(String consumer, List<QueuedMessage> messages) {
        throw new UnsupportedOperationException("练习：逐条分类失败、保留待重试消息，并在业务提交后确认");
    }

    private ProcessingFailure failure(String consumer, QueuedMessage message, FailureKind kind, RuntimeException cause) {
        return new ProcessingFailure(message.id().toString(), cause, policy.recordFailure(consumer, message.id(), kind));
    }

    public record ProcessingFailure(String sourceId, RuntimeException cause, StreamFailurePolicy.Decision decision) {
    }

    public record BatchResult(int applied, int duplicates, int alreadyDeadLettered, List<ProcessingFailure> failures) {
        public BatchResult {
            failures = List.copyOf(failures);
        }

        public long retryPending() {
            return failures.stream().filter(failure -> failure.decision().outcome() == Outcome.RETRY).count();
        }

        public long deadLettered() {
            return failures.stream().filter(failure -> failure.decision().outcome() == Outcome.DEAD_LETTERED).count();
        }
    }
}
