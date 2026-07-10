package io.github.javamastery.exercises.springbootlayers;

public record CourseDto(long id, String title, int lessonCount, String status) {
    public static CourseDto from(Course course) {
        return new CourseDto(course.id(), course.title(), course.lessonCount(), course.status().name());
    }
}
