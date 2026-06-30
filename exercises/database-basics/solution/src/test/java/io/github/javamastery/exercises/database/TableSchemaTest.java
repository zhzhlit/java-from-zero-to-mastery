package io.github.javamastery.exercises.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TableSchema")
class TableSchemaTest {
    @Test
    @DisplayName("生成 CREATE TABLE 语句")
    void createsTableSql() {
        TableSchema schema = courseSchema();

        assertEquals(
                "CREATE TABLE courses (id BIGINT NOT NULL, title VARCHAR(120) NOT NULL, published BOOLEAN)",
                schema.createTableSql()
        );
    }

    @Test
    @DisplayName("找出必填列并按名称查找列")
    void findsRequiredColumnsAndColumnByName() {
        TableSchema schema = courseSchema();

        assertEquals(List.of("id", "title"), schema.requiredColumnNames());
        assertTrue(schema.columnNamed("published").isPresent());
    }

    @Test
    @DisplayName("拒绝空列和重复列")
    void rejectsInvalidSchemas() {
        assertThrows(IllegalArgumentException.class, () -> new TableSchema("courses", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new TableSchema("courses", List.of(
                new ColumnDefinition("id", "BIGINT", false),
                new ColumnDefinition("id", "BIGINT", false)
        )));
    }

    private TableSchema courseSchema() {
        return new TableSchema("courses", List.of(
                new ColumnDefinition("id", "bigint", false),
                new ColumnDefinition("title", "varchar(120)", false),
                new ColumnDefinition("published", "boolean", true)
        ));
    }
}
