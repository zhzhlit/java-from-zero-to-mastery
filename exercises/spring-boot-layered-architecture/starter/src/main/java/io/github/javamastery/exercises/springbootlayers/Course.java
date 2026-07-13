package io.github.javamastery.exercises.springbootlayers;

public class Course {
    private final long id;
    private final String title;
    private final int lessonCount;
    private CourseStatus status;

    public Course(long id, String title, int lessonCount, CourseStatus status) {
        this.id = id;
        this.title = title;
        this.lessonCount = lessonCount;
        this.status = status;
    }

    public long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public int lessonCount() {
        return lessonCount;
    }

    public CourseStatus status() {
        return status;
    }

    public boolean published() {
        return status == CourseStatus.PUBLISHED;
    }

    public void publish() {
        status = CourseStatus.PUBLISHED;
    }
}
