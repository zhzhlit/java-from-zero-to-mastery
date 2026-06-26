package io.github.javamastery.core.oop;

public class LessonProgress {

    private final CourseLesson lesson;
    private int completedMinutes;

    public LessonProgress(CourseLesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("lesson must not be null");
        }
        this.lesson = lesson;
    }

    public CourseLesson getLesson() {
        return lesson;
    }

    public int getCompletedMinutes() {
        return completedMinutes;
    }

    public void recordStudyMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes must be positive");
        }
        completedMinutes = Math.min(
                lesson.getDurationMinutes(),
                completedMinutes + minutes);
    }

    public int remainingMinutes() {
        return lesson.getDurationMinutes() - completedMinutes;
    }

    public double completionRate() {
        return (double) completedMinutes / lesson.getDurationMinutes();
    }

    public boolean isCompleted() {
        return completedMinutes == lesson.getDurationMinutes();
    }
}
