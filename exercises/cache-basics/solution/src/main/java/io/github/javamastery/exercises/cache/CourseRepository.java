package io.github.javamastery.exercises.cache;

import java.util.Optional;

public interface CourseRepository {
    Optional<Course> findById(long id);

    Course rename(long id, String title);
}
