package io.github.javamastery.exercises.database;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SqlIdentifier")
class SqlIdentifierTest {
    private final SqlIdentifier identifier = new SqlIdentifier();

    @Test
    @Disabled("练习：移除 @Disabled，校验普通 SQL 标识符")
    @DisplayName("校验普通表名和列名")
    void validatesSimpleIdentifiers() {
        assertEquals("course_lessons", identifier.validate(" course_lessons "));
        assertEquals("lesson1", identifier.validate("lesson1"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拼接表名和列名")
    @DisplayName("拼接表名和列名")
    void qualifiesColumnName() {
        assertEquals("courses.title", identifier.qualify("courses", "title"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝不安全标识符")
    @DisplayName("拒绝空白、非法字符和保留字")
    void rejectsUnsafeIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> identifier.validate(""));
        assertThrows(IllegalArgumentException.class, () -> identifier.validate("course-title"));
        assertThrows(IllegalArgumentException.class, () -> identifier.validate("select"));
    }
}
