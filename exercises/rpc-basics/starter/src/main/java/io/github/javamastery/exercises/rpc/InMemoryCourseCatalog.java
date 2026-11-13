package io.github.javamastery.exercises.rpc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryCourseCatalog implements CourseCatalog {
    private final Map<Long, Course> courses;

    public InMemoryCourseCatalog(List<Course> courses) {
        this.courses = courses.stream().collect(Collectors.toUnmodifiableMap(Course::id, Function.identity()));
    }

    @Override
    public Optional<Course> findById(long id) {
        return Optional.ofNullable(courses.get(id));
    }
}
