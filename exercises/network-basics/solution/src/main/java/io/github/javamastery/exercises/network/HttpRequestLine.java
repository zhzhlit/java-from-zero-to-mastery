package io.github.javamastery.exercises.network;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public class HttpRequestLine {
    private static final Set<String> ALLOWED_METHODS = Set.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"
    );

    public String build(String method, String url) {
        String normalizedMethod = normalizeMethod(method);
        URI uri = URI.create(url);
        String path = uri.getRawPath();
        String query = uri.getRawQuery();
        String target = path == null || path.isBlank() ? "/" : path;

        if (query != null && !query.isBlank()) {
            target += "?" + query;
        }

        return normalizedMethod + " " + target + " HTTP/1.1";
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("HTTP method must not be blank");
        }
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        return normalized;
    }
}
