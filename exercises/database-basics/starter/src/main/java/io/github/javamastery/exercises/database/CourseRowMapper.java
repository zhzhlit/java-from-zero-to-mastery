package io.github.javamastery.exercises.database;

import java.util.Map;

public class CourseRowMapper {
    public CourseSummary map(Map<String, Object> row) {
        return new CourseSummary(0, "", 0, false);
    }
}
