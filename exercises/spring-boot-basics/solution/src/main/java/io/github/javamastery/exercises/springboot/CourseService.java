package io.github.javamastery.exercises.springboot;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseService {
    private final Map<Long, CourseDto> courses = new LinkedHashMap<>();
    private long nextId = 4;

    public CourseService() {
        courses.put(1L, new CourseDto(1, "JDBC 基础", 8, true));
        courses.put(2L, new CourseDto(2, "HTTP 基础", 6, true));
        courses.put(3L, new CourseDto(3, "内部草稿", 3, false));
    }

    public List<CourseDto> publishedCourses() {
        return courses.values().stream()
                .filter(CourseDto::published)
                .sorted(Comparator.comparingLong(CourseDto::id))
                .toList();
    }

    public Optional<CourseDto> findById(long id) {
        return Optional.ofNullable(courses.get(id));
    }

    public CourseDto create(CreateCourseRequest request) {
        String title = request.title() == null ? "" : request.title().trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        CourseDto course = new CourseDto(nextId, title, Math.max(0, request.lessonCount()), false);
        courses.put(nextId, course);
        nextId++;
        return course;
    }

    public List<CourseDto> allCourses() {
        return new ArrayList<>(courses.values());
    }
}
