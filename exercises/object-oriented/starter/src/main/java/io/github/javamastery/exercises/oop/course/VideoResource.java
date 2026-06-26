package io.github.javamastery.exercises.oop.course;

public class VideoResource extends LearningResource {

    private final int durationMinutes;
    private final boolean hasCaptions;

    public VideoResource(String title, int durationMinutes, boolean hasCaptions) {
        super(title);
        this.durationMinutes = durationMinutes;
        this.hasCaptions = hasCaptions;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public boolean hasCaptions() {
        return hasCaptions;
    }

    @Override
    public int estimatedMinutes() {
        return 0;
    }

    @Override
    protected String typeLabel() {
        return "";
    }

    @Override
    protected String detailText() {
        return "";
    }
}
