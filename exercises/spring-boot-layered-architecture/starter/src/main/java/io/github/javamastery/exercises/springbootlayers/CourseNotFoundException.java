package io.github.javamastery.exercises.springbootlayers;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(long id) {
        super("course not found: " + id);
    }
}
