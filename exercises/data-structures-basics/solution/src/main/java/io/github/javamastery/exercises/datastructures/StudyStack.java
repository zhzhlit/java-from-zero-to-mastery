package io.github.javamastery.exercises.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudyStack {
    private final List<String> topics = new ArrayList<>();

    public void push(String topic) {
        topics.add(requireTopic(topic));
    }

    public String pop() {
        if (topics.isEmpty()) {
            throw new IllegalStateException("stack is empty");
        }
        return topics.remove(topics.size() - 1);
    }

    public Optional<String> peek() {
        if (topics.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(topics.get(topics.size() - 1));
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
