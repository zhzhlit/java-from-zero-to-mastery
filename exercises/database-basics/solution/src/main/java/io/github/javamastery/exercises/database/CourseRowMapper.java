package io.github.javamastery.exercises.database;

import java.util.Map;

public class CourseRowMapper {
    public CourseSummary map(Map<String, Object> row) {
        if (row == null) {
            throw new IllegalArgumentException("row must not be null");
        }
        return new CourseSummary(
                readLong(row, "id"),
                readString(row, "title"),
                readInt(row, "lesson_count"),
                readBoolean(row, "published")
        );
    }

    private long readLong(Map<String, Object> row, String columnName) {
        Object value = required(row, columnName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException(columnName + " must be a number");
    }

    private int readInt(Map<String, Object> row, String columnName) {
        Object value = required(row, columnName);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException(columnName + " must be a number");
    }

    private String readString(Map<String, Object> row, String columnName) {
        Object value = required(row, columnName);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new IllegalArgumentException(columnName + " must be a non-blank string");
    }

    private boolean readBoolean(Map<String, Object> row, String columnName) {
        Object value = required(row, columnName);
        if (value instanceof Boolean flag) {
            return flag;
        }
        throw new IllegalArgumentException(columnName + " must be a boolean");
    }

    private Object required(Map<String, Object> row, String columnName) {
        if (!row.containsKey(columnName) || row.get(columnName) == null) {
            throw new IllegalArgumentException("missing required column: " + columnName);
        }
        return row.get(columnName);
    }
}
