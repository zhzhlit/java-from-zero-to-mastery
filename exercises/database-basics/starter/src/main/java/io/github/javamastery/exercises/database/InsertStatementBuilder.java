package io.github.javamastery.exercises.database;

import java.util.List;

public class InsertStatementBuilder {
    public InsertStatementBuilder(String tableName) {
    }

    public InsertStatementBuilder value(String columnName, Object value) {
        return this;
    }

    public SqlQuery build() {
        return new SqlQuery("", List.of());
    }
}
