package io.github.javamastery.core.oop;

public abstract class LearningResource {

    private final String title;

    protected LearningResource(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.title = title.strip();
    }

    public final String getTitle() {
        return title;
    }

    public final String summary() {
        return typeLabel() + ": " + title + " - " + detailText();
    }

    public abstract int estimatedMinutes();

    protected abstract String typeLabel();

    protected abstract String detailText();
}
