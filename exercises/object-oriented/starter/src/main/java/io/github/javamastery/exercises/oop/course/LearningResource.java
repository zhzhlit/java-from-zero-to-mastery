package io.github.javamastery.exercises.oop.course;

public abstract class LearningResource {

    private final String title;

    protected LearningResource(String title) {
        this.title = title;
    }

    public final String getTitle() {
        return title;
    }

    public final String summary() {
        return "";
    }

    public abstract int estimatedMinutes();

    protected abstract String typeLabel();

    protected abstract String detailText();
}
