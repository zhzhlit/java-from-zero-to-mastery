package io.github.javamastery.exercises.messaging;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PublicationRelay {
    private final PublicationOutbox outbox;
    private final CourseEventQueue queue;

    public PublicationRelay(PublicationOutbox outbox, CourseEventQueue queue) {
        this.outbox = Objects.requireNonNull(outbox);
        this.queue = Objects.requireNonNull(queue);
        if (!outbox.streamName().equals(queue.streamName())) {
            throw new IllegalArgumentException("Outbox 与队列的目标 Stream 必须一致");
        }
    }

    public synchronized RelayResult relayBatch(int limit) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("投递器不能在业务事务中运行");
        }
        int published = 0;
        List<PublicationFailure> failures = new ArrayList<>();
        for (PublicationTask task : outbox.pendingBatch(limit)) {
            StreamEntryID id;
            try {
                id = queue.publish(task.event());
            } catch (JedisException publishFailure) {
                try {
                    outbox.markFailed(task.event().eventId());
                } catch (RuntimeException databaseFailure) {
                    databaseFailure.addSuppressed(publishFailure);
                    throw databaseFailure;
                }
                failures.add(new PublicationFailure(task.event().eventId(), publishFailure));
                continue;
            }
            // XADD may already be visible. A database acknowledgement failure must remain replayable.
            outbox.markPublished(task.event().eventId(), id);
            published++;
        }
        return new RelayResult(published, failures);
    }

    public record PublicationFailure(UUID eventId, JedisException cause) {
    }

    public record RelayResult(int published, List<PublicationFailure> failures) {
        public RelayResult {
            failures = List.copyOf(failures);
        }
    }
}
