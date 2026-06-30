package io.github.javamastery.exercises.database;

public class ColumnDefinition {
    private final String name;
    private final String type;
    private final boolean nullable;

    public ColumnDefinition(String name, String type, boolean nullable) {
        this.name = name;
        this.type = type;
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
        return "";
    }
}
