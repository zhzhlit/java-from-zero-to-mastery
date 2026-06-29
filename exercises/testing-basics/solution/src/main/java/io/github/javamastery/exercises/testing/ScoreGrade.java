package io.github.javamastery.exercises.testing;

public enum ScoreGrade {
    EXCELLENT,
    PASS,
    RETRY;

    public static ScoreGrade fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        if (score >= 90) {
            return EXCELLENT;
        }
        if (score >= 60) {
            return PASS;
        }
        return RETRY;
    }
}
