package io.github.javamastery.exercises.rpc;

import java.util.Optional;

@FunctionalInterface
public interface CourseCatalog {
    Optional<Course> findById(long id);
}
