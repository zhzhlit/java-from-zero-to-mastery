package io.github.javamastery.exercises.oop.course;

public class CodeReviewResult implements ReviewResult {

    private final String title;
    private final int passedChecks;
    private final int totalChecks;

    public CodeReviewResult(String title, int passedChecks, int totalChecks) {
        this.title = title;
        this.passedChecks = passedChecks;
        this.totalChecks = totalChecks;
    }

    public int getPassedChecks() {
        return passedChecks;
    }

    public int getTotalChecks() {
        return totalChecks;
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
