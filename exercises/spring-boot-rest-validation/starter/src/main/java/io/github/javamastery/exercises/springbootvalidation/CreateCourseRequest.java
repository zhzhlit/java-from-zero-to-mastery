package io.github.javamastery.exercises.springbootvalidation;

public record CreateCourseRequest(String title, int lessonCount, String level) {
}
