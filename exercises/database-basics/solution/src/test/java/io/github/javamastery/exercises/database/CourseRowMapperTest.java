package io.github.javamastery.exercises.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CourseRowMapper")
class CourseRowMapperTest {
    private final CourseRowMapper mapper = new CourseRowMapper();

    @Test
    @DisplayName("把数据库行映射成课程摘要")
    void mapsRowToCourseSummary() {
        CourseSummary summary = mapper.map(Map.of(
                "id", 42L,
                "title", "数据库基础",
                "lesson_count", 8,
                "published", true
        ));

        assertEquals(42L, summary.id());
        assertEquals("数据库基础", summary.title());
        assertEquals(8, summary.lessonCount());
        assertTrue(summary.published());
    }

    @Test
    @DisplayName("支持不同 Number 子类型")
    void acceptsDifferentNumberTypes() {
        CourseSummary summary = mapper.map(Map.of(
                "id", 7,
                "title", "SQL 入门",
                "lesson_count", 3L,
                "published", false
        ));

        assertEquals(7L, summary.id());
        assertEquals(3, summary.lessonCount());
        assertFalse(summary.published());
    }

    @Test
    @DisplayName("拒绝缺失列和错误类型")
    void rejectsMissingColumnsAndWrongTypes() {
        assertThrows(IllegalArgumentException.class, () -> mapper.map(Map.of(
                "id", 1L,
                "lesson_count", 2,
                "published", true
        )));
        assertThrows(IllegalArgumentException.class, () -> mapper.map(Map.of(
                "id", "1",
                "title", "数据库基础",
                "lesson_count", 2,
                "published", true
        )));
    }
}
