package io.github.javamastery.exercises.oop.course;

public interface ReviewResult {

    String title();

    int score();

    int maxScore();

    default double scoreRate() {
        return (double) score() / maxScore();
    }

    default boolean passed() {
        return scoreRate() >= 0.6;
    }

    default String reportLine() {
        return title() + ": " + score() + "/" + maxScore()
                + " - " + (passed() ? "PASS" : "RETRY");
    }
}
