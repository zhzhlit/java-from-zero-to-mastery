package io.github.javamastery.exercises.messaging;

import redis.clients.jedis.StreamEntryID;

import java.util.List;
import java.util.Objects;

public record ClaimBatch(StreamEntryID nextStart, List<QueuedMessage> messages) {
    public ClaimBatch {
        Objects.requireNonNull(nextStart);
        messages = List.copyOf(messages);
    }
}
