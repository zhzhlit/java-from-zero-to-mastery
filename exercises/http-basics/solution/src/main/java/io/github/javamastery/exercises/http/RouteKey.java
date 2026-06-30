package io.github.javamastery.exercises.http;

import java.util.Locale;

public record RouteKey(String method, String path) {
    public RouteKey {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        method = method.trim().toUpperCase(Locale.ROOT);
    }
}
