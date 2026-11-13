package io.github.javamastery.exercises.rpc;

public record Course(long id, String title) {
    public Course {
        if (id <= 0 || title == null || title.isBlank() || title.length() > 200) {
            throw new IllegalArgumentException("课程 ID 必须为正数，标题长度为 1 到 200");
        }
        title = title.strip();
    }
}
