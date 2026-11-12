package io.github.javamastery.exercises.cache;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCourseRepository implements CourseRepository {
    private final Map<Long, Course> courses = new ConcurrentHashMap<>(Map.of(
            1L, new Course(1, "Java Basics"),
            2L, new Course(2, "Spring Boot"),
            3L, new Course(3, "Cache Basics")));
    private final AtomicLong queries = new AtomicLong();

    @Override
    public Optional<Course> findById(long id) {
        queries.incrementAndGet();
        return Optional.ofNullable(courses.get(id));
    }

    @Override
    public Course rename(long id, String title) {
        Course renamed = new Course(id, title);
        return courses.compute(id, (key, current) -> {
            if (current == null) {
                throw new NoSuchElementException("课程不存在");
            }
            return renamed;
        });
    }

    public long queryCount() {
        return queries.get();
    }
}
