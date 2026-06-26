package io.github.javamastery.exercises.oop.course;

import java.util.Locale;

public class StudyGoal {

    private String title;
    private int targetMinutes;
    private int completedMinutes;

    public StudyGoal(String title, int targetMinutes) {
        this.title = normalizeTitle(title);
        if (targetMinutes <= 0) {
            throw new IllegalArgumentException("target minutes must be positive");
        }
        this.targetMinutes = targetMinutes;
    }

    public String getTitle() {
        return title;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public int getCompletedMinutes() {
        return completedMinutes;
    }

    public void rename(String title) {
        this.title = normalizeTitle(title);
    }

    public void changeTargetMinutes(int targetMinutes) {
        if (targetMinutes <= 0) {
            throw new IllegalArgumentException("target minutes must be positive");
        }
        this.targetMinutes = targetMinutes;
        completedMinutes = Math.min(completedMinutes, targetMinutes);
    }

    public void recordMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes must be positive");
        }
        completedMinutes = Math.min(targetMinutes, completedMinutes + minutes);
    }

    public int remainingMinutes() {
        return targetMinutes - completedMinutes;
    }

    public double completionRate() {
        return (double) completedMinutes / targetMinutes;
    }

    public boolean isAchieved() {
        return completedMinutes == targetMinutes;
    }

    public String summary() {
        return String.format(
                Locale.ROOT,
                "%s: %d/%d min (%.0f%%)",
                title,
                completedMinutes,
                targetMinutes,
                completionRate() * 100);
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title.strip();
    }
}
