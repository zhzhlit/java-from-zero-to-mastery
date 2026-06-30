package io.github.javamastery.exercises.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TableSchema {
    private final String tableName;
    private final List<ColumnDefinition> columns;

    public TableSchema(String tableName, List<ColumnDefinition> columns) {
        this.tableName = new SqlIdentifier().validate(tableName);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("table must contain at least one column");
        }
        Set<String> names = new HashSet<>();
        for (ColumnDefinition column : columns) {
            if (column == null) {
                throw new IllegalArgumentException("column must not be null");
            }
            if (!names.add(column.name())) {
                throw new IllegalArgumentException("duplicate column: " + column.name());
            }
        }
        this.columns = List.copyOf(columns);
    }

    public String createTableSql() {
        String columnSql = columns.stream()
                .map(ColumnDefinition::toSql)
                .collect(Collectors.joining(", "));
        return "CREATE TABLE " + tableName + " (" + columnSql + ")";
    }

    public List<String> requiredColumnNames() {
        List<String> required = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            if (!column.nullable()) {
                required.add(column.name());
            }
        }
        return required;
    }

    public Optional<ColumnDefinition> columnNamed(String name) {
        String validated = new SqlIdentifier().validate(name);
        return columns.stream()
                .filter(column -> column.name().equals(validated))
                .findFirst();
    }
}
