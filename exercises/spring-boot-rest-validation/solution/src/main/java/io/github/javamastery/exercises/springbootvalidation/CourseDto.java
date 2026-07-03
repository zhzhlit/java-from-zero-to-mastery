package io.github.javamastery.exercises.springbootvalidation;

public record CourseDto(long id, String title, int lessonCount, String level, boolean published) {
}
