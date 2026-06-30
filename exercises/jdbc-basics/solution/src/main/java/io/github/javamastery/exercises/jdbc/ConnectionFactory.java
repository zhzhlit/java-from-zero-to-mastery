package io.github.javamastery.exercises.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection openConnection() {
        try {
            return DriverManager.getConnection(config.url(), config.username(), config.password());
        } catch (SQLException exception) {
            throw JdbcAccessException.connectionFailed(config.url(), exception);
        }
    }
}
