package io.github.javamastery.core.oop;

public class CourseLesson {

    private final String title;
    private final int durationMinutes;

    public CourseLesson(String title, int durationMinutes) {
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

    public String toDisplayText() {
        return title + " (" + durationMinutes + " min)";
    }
}
