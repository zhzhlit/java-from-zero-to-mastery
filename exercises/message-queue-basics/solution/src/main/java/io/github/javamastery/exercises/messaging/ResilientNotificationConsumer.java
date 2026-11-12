package io.github.javamastery.exercises.messaging;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;

import java.util.ArrayList;
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
        Objects.requireNonNull(messages);
        if (consumer == null || !consumer.matches("[A-Za-z0-9_-]{1,64}") || messages.size() > 100) {
            throw new IllegalArgumentException("消费者名称不合法或批次过大");
        }
        int applied = 0;
        int duplicates = 0;
        int alreadyDead = 0;
        List<ProcessingFailure> failures = new ArrayList<>();
        for (QueuedMessage message : messages) {
            var beginning = policy.begin(consumer, message.id());
            if (beginning.outcome() == Outcome.ALREADY_DEAD_LETTERED) {
                alreadyDead++;
                continue;
            }
            if (beginning.outcome() != Outcome.READY) {
                throw new IllegalStateException("消息开始状态不合法");
            }
            CoursePublishedEvent event;
            try {
                event = CoursePublishedEvent.fromFields(message.fields());
            } catch (IllegalArgumentException invalid) {
                failures.add(failure(consumer, message, FailureKind.INVALID_EVENT, invalid));
                continue;
            }
            boolean stored;
            try {
                stored = inbox.storeIfNew(event);
            } catch (EventPayloadConflictException conflict) {
                failures.add(failure(consumer, message, FailureKind.EVENT_ID_CONFLICT, conflict));
                continue;
            } catch (DataIntegrityViolationException constraint) {
                failures.add(failure(consumer, message, FailureKind.BUSINESS_CONSTRAINT, constraint));
                continue;
            } catch (TransientDataAccessException transientFailure) {
                failures.add(failure(consumer, message, FailureKind.TRANSIENT_DATABASE, transientFailure));
                continue;
            }
            if (stored) {
                applied++;
            } else {
                duplicates++;
            }
            // Database work has committed. Acknowledgement errors must not become poison-message failures.
            var acknowledgement = policy.acknowledgeSuccess(consumer, message.id());
            if (acknowledgement.outcome() == Outcome.ALREADY_DEAD_LETTERED) {
                alreadyDead++;
            } else if (acknowledgement.outcome() != Outcome.ACKNOWLEDGED) {
                throw new IllegalStateException("消息确认状态不合法");
            }
        }
        return new BatchResult(applied, duplicates, alreadyDead, failures);
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
