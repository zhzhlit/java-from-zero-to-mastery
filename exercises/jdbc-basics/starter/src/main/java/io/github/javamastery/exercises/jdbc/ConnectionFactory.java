package io.github.javamastery.exercises.jdbc;

import java.sql.Connection;

public final class ConnectionFactory {
    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection openConnection() {
        throw new UnsupportedOperationException("TODO: use DriverManager.getConnection with config values");
    }
}
