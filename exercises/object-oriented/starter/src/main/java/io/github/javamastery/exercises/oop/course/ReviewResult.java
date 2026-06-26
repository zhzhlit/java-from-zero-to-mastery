package io.github.javamastery.exercises.oop.course;

public interface ReviewResult {

    String title();

    int score();

    int maxScore();

    default double scoreRate() {
        return 0.0;
    }

    default boolean passed() {
        return false;
    }

    default String reportLine() {
        return "";
    }
}
