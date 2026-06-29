package io.github.javamastery.exercises.testing;

public enum ScoreGrade {
    EXCELLENT,
    PASS,
    RETRY;

    public static ScoreGrade fromScore(int score) {
        return RETRY;
    }
}
