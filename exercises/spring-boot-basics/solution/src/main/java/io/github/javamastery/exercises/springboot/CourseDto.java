package io.github.javamastery.exercises.springboot;

public record CourseDto(long id, String title, int lessonCount, boolean published) {
}
