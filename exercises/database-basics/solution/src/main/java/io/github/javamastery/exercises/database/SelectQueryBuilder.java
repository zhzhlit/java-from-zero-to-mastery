package io.github.javamastery.exercises.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectQueryBuilder {
    private final SqlIdentifier identifier = new SqlIdentifier();
    private final String tableName;
    private final List<String> columns = new ArrayList<>();
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private String orderByColumn;
    private SortDirection orderDirection;
    private Integer limit;
    private Integer offset;

    public SelectQueryBuilder(String tableName) {
        this.tableName = identifier.validate(tableName);
    }

    public SelectQueryBuilder select(String... columnNames) {
        columns.clear();
        if (columnNames != null) {
            Arrays.stream(columnNames)
                    .map(identifier::validate)
                    .forEach(columns::add);
        }
        return this;
    }

    public SelectQueryBuilder whereEquals(String columnName, Object value) {
        conditions.add(identifier.validate(columnName) + " = ?");
        parameters.add(value);
        return this;
    }

    public SelectQueryBuilder whereAtLeast(String columnName, Object value) {
        conditions.add(identifier.validate(columnName) + " >= ?");
        parameters.add(value);
        return this;
    }

    public SelectQueryBuilder orderBy(String columnName, SortDirection direction) {
        orderByColumn = identifier.validate(columnName);
        orderDirection = direction == null ? SortDirection.ASC : direction;
        return this;
    }

    public SelectQueryBuilder limit(int rowCount) {
        if (rowCount < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        limit = rowCount;
        return this;
    }

    public SelectQueryBuilder offset(int skippedRows) {
        if (skippedRows < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        offset = skippedRows;
        return this;
    }

    public SqlQuery build() {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(columns.isEmpty() ? "*" : String.join(", ", columns));
        sql.append(" FROM ").append(tableName);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (orderByColumn != null) {
            sql.append(" ORDER BY ").append(orderByColumn).append(" ").append(orderDirection);
        }
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }

        return new SqlQuery(sql.toString(), parameters);
    }
}
