package io.github.javamastery.exercises.jdbc;

public final class JdbcAccessException extends RuntimeException {
    private JdbcAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public static JdbcAccessException connectionFailed(String url, Throwable cause) {
        return new JdbcAccessException("Could not open JDBC connection: " + url, cause);
    }

    public static JdbcAccessException queryFailed(String operation, Throwable cause) {
        return new JdbcAccessException("JDBC operation failed: " + operation, cause);
    }
}
