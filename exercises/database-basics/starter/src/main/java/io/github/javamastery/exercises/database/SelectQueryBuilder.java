package io.github.javamastery.exercises.database;

import java.util.List;

public class SelectQueryBuilder {
    public SelectQueryBuilder(String tableName) {
    }

    public SelectQueryBuilder select(String... columnNames) {
        return this;
    }

    public SelectQueryBuilder whereEquals(String columnName, Object value) {
        return this;
    }

    public SelectQueryBuilder whereAtLeast(String columnName, Object value) {
        return this;
    }

    public SelectQueryBuilder orderBy(String columnName, SortDirection direction) {
        return this;
    }

    public SelectQueryBuilder limit(int rowCount) {
        return this;
    }

    public SelectQueryBuilder offset(int skippedRows) {
        return this;
    }

    public SqlQuery build() {
        return new SqlQuery("", List.of());
    }
}
