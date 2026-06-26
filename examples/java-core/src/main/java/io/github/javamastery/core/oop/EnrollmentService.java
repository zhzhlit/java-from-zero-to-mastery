package io.github.javamastery.core.oop;

public class EnrollmentService {

    private final CourseLesson lesson;
    private final int capacity;
    private int enrolledCount;

    public EnrollmentService(CourseLesson lesson, int capacity) {
        if (lesson == null) {
            throw new IllegalArgumentException("lesson must not be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.lesson = lesson;
        this.capacity = capacity;
    }

    public CourseLesson getLesson() {
        return lesson;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public int remainingSeats() {
        return capacity - enrolledCount;
    }

    public void enroll(String studentName) {
        enrollAndReturnName(studentName);
    }

    public String tryEnroll(String studentName) {
        try {
            String normalizedName = enrollAndReturnName(studentName);
            return normalizedName + " enrolled in " + lesson.getTitle();
        } catch (IllegalArgumentException | EnrollmentException exception) {
            return "Enrollment failed: " + exception.getMessage();
        }
    }

    private String enrollAndReturnName(String studentName) {
        String normalizedName = normalizeStudentName(studentName);
        if (enrolledCount >= capacity) {
            throw new EnrollmentException("course is full: " + lesson.getTitle());
        }
        enrolledCount++;
        return normalizedName;
    }

    private static String normalizeStudentName(String studentName) {
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("student name must not be blank");
        }
        return studentName.strip();
    }
}
