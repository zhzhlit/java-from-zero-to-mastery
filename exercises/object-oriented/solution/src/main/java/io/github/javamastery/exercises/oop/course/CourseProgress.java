package io.github.javamastery.exercises.oop.course;

import java.util.Locale;

public class CourseProgress {

    private final String studentName;
    private final Lesson lesson;
    private int completedMinutes;

    public CourseProgress(String studentName, Lesson lesson) {
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("student name must not be blank");
        }
        if (lesson == null) {
            throw new IllegalArgumentException("lesson must not be null");
        }
        this.studentName = studentName.strip();
        this.lesson = lesson;
    }

    public String getStudentName() {
        return studentName;
    }

    public Lesson getLesson() {
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

    public String summary() {
        return String.format(
                Locale.ROOT,
                "%s completed %d/%d min of %s (%.0f%%)",
                studentName,
                completedMinutes,
                lesson.getDurationMinutes(),
                lesson.getTitle(),
                completionRate() * 100);
    }
}
