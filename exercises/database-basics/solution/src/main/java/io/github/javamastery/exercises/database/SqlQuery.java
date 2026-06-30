package io.github.javamastery.exercises.database;

import java.util.List;

public class SqlQuery {
    private final String sql;
    private final List<Object> parameters;

    public SqlQuery(String sql, List<Object> parameters) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL must not be blank");
        }
        this.sql = sql;
        this.parameters = List.copyOf(parameters);
    }

    public String sql() {
        return sql;
    }

    public List<Object> parameters() {
        return parameters;
    }
}
