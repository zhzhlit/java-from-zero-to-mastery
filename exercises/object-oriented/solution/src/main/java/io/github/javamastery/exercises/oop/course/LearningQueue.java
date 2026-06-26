package io.github.javamastery.exercises.oop.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearningQueue<T> {

    private final List<T> items = new ArrayList<>();

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        items.add(item);
    }

    public T peekNext() {
        if (items.isEmpty()) {
            throw new IllegalStateException("queue is empty");
        }
        return items.get(0);
    }

    public T completeNext() {
        if (items.isEmpty()) {
            throw new IllegalStateException("queue is empty");
        }
        return items.remove(0);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<T> remainingItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
