package io.github.javamastery.core.oop;

public class QuizResult implements ReviewResult {

    private final String title;
    private final int correctAnswers;
    private final int totalQuestions;

    public QuizResult(String title, int correctAnswers, int totalQuestions) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (totalQuestions <= 0) {
            throw new IllegalArgumentException("total questions must be positive");
        }
        if (correctAnswers < 0 || correctAnswers > totalQuestions) {
            throw new IllegalArgumentException("correct answers must be within total questions");
        }
        this.title = title.strip();
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public int score() {
        return correctAnswers;
    }

    @Override
    public int maxScore() {
        return totalQuestions;
    }
}
