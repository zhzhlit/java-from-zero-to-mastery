package io.github.javamastery.exercises.datastructures;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Optional;

public class StudyQueue {
    private final Queue<String> topics = new ArrayDeque<>();

    public void offer(String topic) {
        topics.offer(requireTopic(topic));
    }

    public String poll() {
        String topic = topics.poll();
        if (topic == null) {
            throw new IllegalStateException("queue is empty");
        }
        return topic;
    }

    public Optional<String> peek() {
        return Optional.ofNullable(topics.peek());
    }

    public int size() {
        return topics.size();
    }

    public boolean isEmpty() {
        return topics.isEmpty();
    }

    private static String requireTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic must not be blank");
        }
        return topic.strip();
    }
}
