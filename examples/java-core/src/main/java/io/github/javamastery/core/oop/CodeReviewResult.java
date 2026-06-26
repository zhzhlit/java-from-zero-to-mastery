package io.github.javamastery.core.oop;

public class CodeReviewResult implements ReviewResult {

    private final String title;
    private final int passedChecks;
    private final int totalChecks;

    public CodeReviewResult(String title, int passedChecks, int totalChecks) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (totalChecks <= 0) {
            throw new IllegalArgumentException("total checks must be positive");
        }
        if (passedChecks < 0 || passedChecks > totalChecks) {
            throw new IllegalArgumentException("passed checks must be within total checks");
        }
        this.title = title.strip();
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
        return passedChecks;
    }

    @Override
    public int maxScore() {
        return totalChecks;
    }
}
