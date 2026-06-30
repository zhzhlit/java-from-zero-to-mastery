package io.github.javamastery.exercises.springboot;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    public List<CourseDto> publishedCourses() {
        return List.of();
    }

    public Optional<CourseDto> findById(long id) {
        return Optional.empty();
    }

    public CourseDto create(CreateCourseRequest request) {
        return new CourseDto(1, request.title(), request.lessonCount(), false);
    }
}
