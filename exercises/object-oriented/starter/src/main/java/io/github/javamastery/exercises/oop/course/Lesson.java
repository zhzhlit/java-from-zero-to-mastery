package io.github.javamastery.exercises.oop.course;

public class Lesson {

    private final String title;
    private final int durationMinutes;

    public Lesson(String title, int durationMinutes) {
        this.title = title;
        this.durationMinutes = durationMinutes;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
