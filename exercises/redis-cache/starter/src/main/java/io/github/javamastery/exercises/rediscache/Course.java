package io.github.javamastery.exercises.rediscache;

public record Course(long id, String title) {
    public Course {
        if (id <= 0 || title == null || title.isBlank()) {
            throw new IllegalArgumentException("课程 ID 必须为正数，标题不能为空");
        }
        title = title.trim();
    }
}
