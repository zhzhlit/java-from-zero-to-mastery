package io.github.javamastery.exercises.database;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InsertStatementBuilder")
class InsertStatementBuilderTest {
    @Test
    @Disabled("练习：移除 @Disabled，构造 INSERT")
    @DisplayName("构造按插入顺序保存参数的 INSERT")
    void buildsInsertStatementWithParametersInOrder() {
        SqlQuery query = new InsertStatementBuilder("courses")
                .value("title", "Java 入门")
                .value("lesson_count", 12)
                .value("published", false)
                .build();

        assertEquals("INSERT INTO courses (title, lesson_count, published) VALUES (?, ?, ?)", query.sql());
        assertEquals(List.of("Java 入门", 12, false), query.parameters());
    }

    @Test
    @Disabled("练习：移除 @Disabled，处理重复列")
    @DisplayName("重复列使用最后一次值但保持列顺序")
    void replacesRepeatedColumnValue() {
        SqlQuery query = new InsertStatementBuilder("courses")
                .value("title", "旧标题")
                .value("title", "新标题")
                .build();

        assertEquals("INSERT INTO courses (title) VALUES (?)", query.sql());
        assertEquals(List.of("新标题"), query.parameters());
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝空 INSERT")
    @DisplayName("拒绝没有任何值的 INSERT")
    void rejectsEmptyInsert() {
        InsertStatementBuilder builder = new InsertStatementBuilder("courses");

        assertThrows(IllegalStateException.class, builder::build);
    }
}
