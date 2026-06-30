package io.github.javamastery.exercises.database;

import java.util.List;
import java.util.Optional;

public class TableSchema {
    public TableSchema(String tableName, List<ColumnDefinition> columns) {
    }

    public String createTableSql() {
        return "";
    }

    public List<String> requiredColumnNames() {
        return List.of();
    }

    public Optional<ColumnDefinition> columnNamed(String name) {
        return Optional.empty();
    }
}
