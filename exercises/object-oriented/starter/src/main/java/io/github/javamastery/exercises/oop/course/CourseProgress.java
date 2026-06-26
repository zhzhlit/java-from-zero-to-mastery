package io.github.javamastery.exercises.oop.course;

public class CourseProgress {

    private final String studentName;
    private final Lesson lesson;
    private int completedMinutes;

    public CourseProgress(String studentName, Lesson lesson) {
        this.studentName = studentName;
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
    }

    public int remainingMinutes() {
        return 0;
    }

    public double completionRate() {
        return 0.0;
    }

    public boolean isCompleted() {
        return false;
    }

    public String summary() {
        return "";
    }
}
