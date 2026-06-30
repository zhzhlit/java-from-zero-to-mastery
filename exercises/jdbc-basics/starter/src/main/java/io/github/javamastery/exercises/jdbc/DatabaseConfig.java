package io.github.javamastery.exercises.jdbc;

import java.util.Objects;

public record DatabaseConfig(String url, String username, String password) {
    public DatabaseConfig {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }
        username = Objects.requireNonNullElse(username, "");
        password = Objects.requireNonNullElse(password, "");
    }
}
