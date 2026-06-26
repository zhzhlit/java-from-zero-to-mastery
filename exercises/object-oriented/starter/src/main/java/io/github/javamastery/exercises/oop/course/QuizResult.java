package io.github.javamastery.exercises.oop.course;

public class QuizResult implements ReviewResult {

    private final String title;
    private final int correctAnswers;
    private final int totalQuestions;

    public QuizResult(String title, int correctAnswers, int totalQuestions) {
        this.title = title;
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
        return 0;
    }

    @Override
    public int maxScore() {
        return 0;
    }
}
