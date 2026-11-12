package io.github.javamastery.exercises.rediscache;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.NoSuchElementException;
import java.util.Optional;

public class JdbcCourseRepository implements CourseRepository {
    private final JdbcTemplate jdbc;

    public JdbcCourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Course> findById(long id) {
        return jdbc.query("SELECT id, title FROM courses WHERE id = ?",
                (rs, rowNum) -> new Course(rs.getLong("id"), rs.getString("title")), id).stream().findFirst();
    }

    @Override
    public Course rename(long id, String title) {
        Course updated = new Course(id, title);
        if (jdbc.update("UPDATE courses SET title = ? WHERE id = ?", updated.title(), id) != 1) {
            throw new NoSuchElementException("课程不存在");
        }
        return updated;
    }
}
