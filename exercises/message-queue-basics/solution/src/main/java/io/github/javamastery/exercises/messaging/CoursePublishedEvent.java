package io.github.javamastery.exercises.messaging;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CoursePublishedEvent(UUID eventId, long courseId, String title) {
    public CoursePublishedEvent {
        Objects.requireNonNull(eventId);
        if (courseId <= 0 || title == null || title.isBlank() || title.length() > 200) {
            throw new IllegalArgumentException("课程 ID 必须为正数，标题长度为 1 到 200");
        }
        title = title.trim();
    }

    public Map<String, String> toFields() {
        return Map.of("schemaVersion", "1", "eventId", eventId.toString(),
                "courseId", Long.toString(courseId), "title", title);
    }

    public static CoursePublishedEvent fromFields(Map<String, String> fields) {
        Objects.requireNonNull(fields);
        if (!"1".equals(required(fields, "schemaVersion"))) {
            throw new IllegalArgumentException("不支持的事件版本");
        }
        return new CoursePublishedEvent(UUID.fromString(required(fields, "eventId")),
                Long.parseLong(required(fields, "courseId")), required(fields, "title"));
    }

    private static String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("事件缺少字段：" + name);
        }
        return value;
    }
}
