package io.github.javamastery.exercises.messaging;

import redis.clients.jedis.exceptions.JedisException;

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
        throw new UnsupportedOperationException("练习：提交后逐条投递，成功后标记，失败保留并报告");
    }

    public record PublicationFailure(UUID eventId, JedisException cause) {
    }

    public record RelayResult(int published, List<PublicationFailure> failures) {
        public RelayResult {
            failures = List.copyOf(failures);
        }
    }
}
