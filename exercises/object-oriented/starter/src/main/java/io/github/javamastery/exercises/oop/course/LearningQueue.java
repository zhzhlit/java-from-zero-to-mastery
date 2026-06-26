package io.github.javamastery.exercises.oop.course;

import java.util.ArrayList;
import java.util.List;

public class LearningQueue<T> {

    private final List<T> items = new ArrayList<>();

    public void add(T item) {
    }

    public T peekNext() {
        return null;
    }

    public T completeNext() {
        return null;
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }

    public List<T> remainingItems() {
        return List.of();
    }
}
