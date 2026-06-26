package io.github.javamastery.flagship.cli;

import java.util.Locale;

public class Lesson {

    private final String id;
    private final String title;
    private final int durationMinutes;

    public Lesson(String id, String title, int durationMinutes) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration minutes must be positive");
        }
        this.id = normalizeId(id);
        this.title = title.strip();
        this.durationMinutes = durationMinutes;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public int durationMinutes() {
        return durationMinutes;
    }

    public String displayText() {
        return id + " - " + title + " (" + durationMinutes + " min)";
    }

    static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("lesson id must not be blank");
        }
        String normalized = id.strip().toLowerCase(Locale.ROOT);
        if (normalized.contains(",")) {
            throw new IllegalArgumentException("lesson id must not contain comma");
        }
        return normalized;
    }
}
