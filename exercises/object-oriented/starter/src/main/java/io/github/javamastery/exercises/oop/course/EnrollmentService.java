package io.github.javamastery.exercises.oop.course;

public class EnrollmentService {

    private final Lesson lesson;
    private final int capacity;
    private int enrolledCount;

    public EnrollmentService(Lesson lesson, int capacity) {
        this.lesson = lesson;
        this.capacity = capacity;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public int remainingSeats() {
        return 0;
    }

    public void enroll(String studentName) {
    }

    public String tryEnroll(String studentName) {
        return "";
    }
}
