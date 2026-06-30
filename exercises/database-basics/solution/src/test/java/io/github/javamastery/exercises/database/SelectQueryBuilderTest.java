package io.github.javamastery.exercises.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SelectQueryBuilder")
class SelectQueryBuilderTest {
    @Test
    @DisplayName("构造带条件、排序和分页的参数化 SELECT")
    void buildsParameterizedSelectQuery() {
        SqlQuery query = new SelectQueryBuilder("courses")
                .select("id", "title")
                .whereEquals("published", true)
                .whereAtLeast("lesson_count", 5)
                .orderBy("title", SortDirection.ASC)
                .limit(20)
                .offset(40)
                .build();

        assertEquals(
                "SELECT id, title FROM courses WHERE published = ? AND lesson_count >= ? ORDER BY title ASC LIMIT 20 OFFSET 40",
                query.sql()
        );
        assertEquals(List.of(true, 5), query.parameters());
    }

    @Test
    @DisplayName("没有指定列时查询全部列")
    void selectsAllColumnsByDefault() {
        SqlQuery query = new SelectQueryBuilder("courses")
                .whereEquals("status", "DRAFT")
                .build();

        assertEquals("SELECT * FROM courses WHERE status = ?", query.sql());
        assertEquals(List.of("DRAFT"), query.parameters());
    }

    @Test
    @DisplayName("拒绝无效分页参数")
    void rejectsInvalidPagination() {
        SelectQueryBuilder builder = new SelectQueryBuilder("courses");

        assertThrows(IllegalArgumentException.class, () -> builder.limit(0));
        assertThrows(IllegalArgumentException.class, () -> builder.offset(-1));
    }
}
