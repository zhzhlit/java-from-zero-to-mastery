package io.github.javamastery.exercises.search;

import java.util.Objects;

public record CourseDocument(long id, String title, String description, CourseLevel level) {
    public CourseDocument {
        Objects.requireNonNull(level);
        if (id <= 0 || title == null || title.isBlank() || title.length() > 200
                || description == null || description.isBlank() || description.length() > 4000) {
            throw new IllegalArgumentException("课程 ID 必须为正数，标题为 1 到 200 字符，描述为 1 到 4000 字符");
        }
        title = title.strip();
        description = description.strip();
    }
}
