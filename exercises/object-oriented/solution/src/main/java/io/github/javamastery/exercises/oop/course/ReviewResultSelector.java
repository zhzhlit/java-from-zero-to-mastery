package io.github.javamastery.exercises.oop.course;

import java.util.List;

public final class ReviewResultSelector {

    private ReviewResultSelector() {
    }

    public static <T extends ReviewResult> T bestResult(List<T> results) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("results must not be empty");
        }

        T best = requireResult(results.get(0));
        for (int index = 1; index < results.size(); index++) {
            T current = requireResult(results.get(index));
            if (current.scoreRate() > best.scoreRate()) {
                best = current;
            }
        }
        return best;
    }

    private static <T extends ReviewResult> T requireResult(T result) {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        return result;
    }
}
