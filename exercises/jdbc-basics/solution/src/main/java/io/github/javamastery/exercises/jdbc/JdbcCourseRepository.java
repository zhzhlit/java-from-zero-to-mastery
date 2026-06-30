package io.github.javamastery.exercises.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcCourseRepository {
    private final ConnectionFactory connectionFactory;
    private final CourseRowMapper rowMapper = new CourseRowMapper();

    public JdbcCourseRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<CourseRecord> findById(long id) {
        String sql = """
                SELECT id, title, lesson_count, published
                FROM courses
                WHERE id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(rowMapper.map(resultSet));
            }
        } catch (SQLException exception) {
            throw JdbcAccessException.queryFailed("find course by id", exception);
        }
    }

    public List<CourseRecord> findPublished(int minimumLessons) {
        String sql = """
                SELECT id, title, lesson_count, published
                FROM courses
                WHERE published = ? AND lesson_count >= ?
                ORDER BY title
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, true);
            statement.setInt(2, minimumLessons);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CourseRecord> courses = new ArrayList<>();
                while (resultSet.next()) {
                    courses.add(rowMapper.map(resultSet));
                }
                return List.copyOf(courses);
            }
        } catch (SQLException exception) {
            throw JdbcAccessException.queryFailed("find published courses", exception);
        }
    }

    public int create(CourseRecord course) {
        String sql = """
                INSERT INTO courses (id, title, lesson_count, published)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, course.id());
            statement.setString(2, course.title());
            statement.setInt(3, course.lessonCount());
            statement.setBoolean(4, course.published());
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw JdbcAccessException.queryFailed("create course", exception);
        }
    }
}
