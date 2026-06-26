package io.github.javamastery.exercises.oop.course;

public class StudyGoal {

    private String title;
    private int targetMinutes;
    private int completedMinutes;

    public StudyGoal(String title, int targetMinutes) {
        this.title = title;
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
    }

    public void changeTargetMinutes(int targetMinutes) {
    }

    public void recordMinutes(int minutes) {
    }

    public int remainingMinutes() {
        return 0;
    }

    public double completionRate() {
        return 0.0;
    }

    public boolean isAchieved() {
        return false;
    }

    public String summary() {
        return "";
    }
}
