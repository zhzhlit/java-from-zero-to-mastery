package io.github.javamastery.exercises.jdbc;

public record CourseRecord(long id, String title, int lessonCount, boolean published) {
    public CourseRecord {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (lessonCount < 0) {
            throw new IllegalArgumentException("lessonCount must not be negative");
        }
    }
}
