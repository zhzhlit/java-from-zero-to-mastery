package io.github.javamastery.exercises.webreview;

public record CourseSummary(long id, String title, int lessonCount, boolean published) {
    public CourseSummary {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (lessonCount < 0) {
            throw new IllegalArgumentException("lesson count must not be negative");
        }
    }
}
