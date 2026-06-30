package io.github.javamastery.exercises.jdbc;

import java.util.List;
import java.util.Optional;

public final class JdbcCourseRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcCourseRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<CourseRecord> findById(long id) {
        throw new UnsupportedOperationException("TODO: query by id with PreparedStatement and map one row");
    }

    public List<CourseRecord> findPublished(int minimumLessons) {
        throw new UnsupportedOperationException("TODO: query published courses with parameters and map rows");
    }

    public int create(CourseRecord course) {
        throw new UnsupportedOperationException("TODO: insert a course with PreparedStatement parameters");
    }
}
