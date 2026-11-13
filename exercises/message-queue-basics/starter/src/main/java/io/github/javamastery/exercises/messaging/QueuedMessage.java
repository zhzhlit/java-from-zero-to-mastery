package io.github.javamastery.exercises.messaging;

import redis.clients.jedis.StreamEntryID;

import java.util.Map;
import java.util.Objects;

public record QueuedMessage(StreamEntryID id, Map<String, String> fields) {
    public QueuedMessage {
        Objects.requireNonNull(id);
        // Deleted pending entries can retain an ID without a payload; decoding must fail, not auto-ack.
        fields = fields == null ? Map.of() : Map.copyOf(fields);
    }
}
