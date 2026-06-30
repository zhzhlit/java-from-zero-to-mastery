package io.github.javamastery.exercises.database;

public class ColumnDefinition {
    private final String name;
    private final String type;
    private final boolean nullable;

    public ColumnDefinition(String name, String type, boolean nullable) {
        SqlIdentifier identifier = new SqlIdentifier();
        this.name = identifier.validate(name);
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("column type must not be blank");
        }
        this.type = type.trim().toUpperCase();
        this.nullable = nullable;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public boolean nullable() {
        return nullable;
    }

    public String toSql() {
        if (nullable) {
            return name + " " + type;
        }
        return name + " " + type + " NOT NULL";
    }
}
