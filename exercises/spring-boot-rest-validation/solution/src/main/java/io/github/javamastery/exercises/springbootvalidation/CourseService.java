package io.github.javamastery.exercises.springbootvalidation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CourseService {
    private final AtomicLong idSequence = new AtomicLong(3);
    private final List<CourseDto> courses = new ArrayList<>(List.of(
            new CourseDto(1, "Spring Boot 基础", 10, "beginner", true),
            new CourseDto(2, "Spring Boot 配置", 8, "intermediate", true),
            new CourseDto(3, "REST API 校验", 6, "intermediate", false)
    ));

    public List<CourseDto> publishedCourses() {
        return courses.stream()
                .filter(CourseDto::published)
                .sorted(Comparator.comparingLong(CourseDto::id))
                .toList();
    }

    public Optional<CourseDto> findById(long id) {
        return courses.stream()
                .filter(course -> course.id() == id)
                .findFirst();
    }

    public CourseDto create(CreateCourseRequest request) {
        CourseDto course = new CourseDto(
                idSequence.incrementAndGet(),
                request.title().trim(),
                request.lessonCount(),
                request.level(),
                false
        );
        courses.add(course);
        return course;
    }
}
