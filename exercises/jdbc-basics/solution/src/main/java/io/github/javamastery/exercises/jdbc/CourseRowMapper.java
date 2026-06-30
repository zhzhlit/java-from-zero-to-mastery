package io.github.javamastery.exercises.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class CourseRowMapper {
    public CourseRecord map(ResultSet resultSet) throws SQLException {
        return new CourseRecord(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getInt("lesson_count"),
                resultSet.getBoolean("published")
        );
    }
}
