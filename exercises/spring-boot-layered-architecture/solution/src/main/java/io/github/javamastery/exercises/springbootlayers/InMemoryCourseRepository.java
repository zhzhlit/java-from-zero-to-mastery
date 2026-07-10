package io.github.javamastery.exercises.springbootlayers;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCourseRepository implements CourseRepository {
    private final AtomicLong idSequence = new AtomicLong(3);
    private final Map<Long, Course> courses = new ConcurrentHashMap<>(Map.of(
            1L, new Course(1, "Spring Boot 基础", 10, CourseStatus.PUBLISHED),
            2L, new Course(2, "Spring Boot 配置", 8, CourseStatus.PUBLISHED),
            3L, new Course(3, "REST API 校验", 6, CourseStatus.DRAFT)
    ));

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses.values()).stream()
                .sorted(Comparator.comparingLong(Course::id))
                .toList();
    }

    @Override
    public Optional<Course> findById(long id) {
        return Optional.ofNullable(courses.get(id));
    }

    @Override
    public boolean existsByTitleIgnoreCase(String title) {
        return courses.values().stream()
                .anyMatch(course -> course.title().equalsIgnoreCase(title));
    }

    @Override
    public Course save(Course course) {
        courses.put(course.id(), course);
        return course;
    }

    @Override
    public long nextId() {
        return idSequence.incrementAndGet();
    }
}
