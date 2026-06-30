package io.github.javamastery.exercises.os;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class RoundRobinScheduler {
    public List<String> executionOrder(Map<String, Integer> remainingMillisByProcess, int quantumMillis) {
        if (remainingMillisByProcess == null) {
            throw new IllegalArgumentException("process map must not be null");
        }
        if (quantumMillis <= 0) {
            throw new IllegalArgumentException("quantum must be positive");
        }

        Queue<ProcessWork> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : remainingMillisByProcess.entrySet()) {
            String name = entry.getKey();
            Integer remainingMillis = entry.getValue();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("process name must not be blank");
            }
            if (remainingMillis == null || remainingMillis < 0) {
                throw new IllegalArgumentException("remaining time must not be negative");
            }
            if (remainingMillis > 0) {
                queue.add(new ProcessWork(name, remainingMillis));
            }
        }

        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            ProcessWork current = queue.remove();
            order.add(current.name);
            int remainingAfterSlice = current.remainingMillis - quantumMillis;
            if (remainingAfterSlice > 0) {
                queue.add(new ProcessWork(current.name, remainingAfterSlice));
            }
        }
        return order;
    }

    private static class ProcessWork {
        private final String name;
        private final int remainingMillis;

        private ProcessWork(String name, int remainingMillis) {
            this.name = name;
            this.remainingMillis = remainingMillis;
        }
    }
}
