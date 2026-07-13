package io.github.javamastery.exercises.springbootlayers;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    List<Course> findAll();

    Optional<Course> findById(long id);

    boolean existsByTitleIgnoreCase(String title);

    Course save(Course course);

    long nextId();
}
