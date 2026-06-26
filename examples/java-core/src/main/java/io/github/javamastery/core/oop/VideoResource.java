package io.github.javamastery.core.oop;

public class VideoResource extends LearningResource {

    private final int durationMinutes;
    private final boolean hasCaptions;

    public VideoResource(String title, int durationMinutes, boolean hasCaptions) {
        super(title);
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration minutes must be positive");
        }
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
        return durationMinutes;
    }

    @Override
    protected String typeLabel() {
        return "Video";
    }

    @Override
    protected String detailText() {
        return durationMinutes + " min, "
                + (hasCaptions ? "captions available" : "no captions");
    }
}
