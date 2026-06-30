package io.github.javamastery.exercises.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertStatementBuilder {
    private final SqlIdentifier identifier = new SqlIdentifier();
    private final String tableName;
    private final Map<String, Object> values = new LinkedHashMap<>();

    public InsertStatementBuilder(String tableName) {
        this.tableName = identifier.validate(tableName);
    }

    public InsertStatementBuilder value(String columnName, Object value) {
        values.put(identifier.validate(columnName), value);
        return this;
    }

    public SqlQuery build() {
        if (values.isEmpty()) {
            throw new IllegalStateException("insert must contain at least one value");
        }

        String columns = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream()
                .map(ignored -> "?")
                .collect(Collectors.joining(", "));
        List<Object> parameters = new ArrayList<>(values.values());
        return new SqlQuery(
                "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")",
                parameters
        );
    }
}
