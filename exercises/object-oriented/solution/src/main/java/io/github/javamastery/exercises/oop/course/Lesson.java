package io.github.javamastery.exercises.oop.course;

public class Lesson {

    private final String title;
    private final int durationMinutes;

    public Lesson(String title, int durationMinutes) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration minutes must be positive");
        }
        this.title = title.strip();
        this.durationMinutes = durationMinutes;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
