package io.github.javamastery.exercises.database;

public class CourseSummary {
    private final long id;
    private final String title;
    private final int lessonCount;
    private final boolean published;

    public CourseSummary(long id, String title, int lessonCount, boolean published) {
        this.id = id;
        this.title = title;
        this.lessonCount = lessonCount;
        this.published = published;
    }

    public long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public int lessonCount() {
        return lessonCount;
    }

    public boolean published() {
        return published;
    }
}
