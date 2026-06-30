package io.github.javamastery.exercises.http;

import java.util.LinkedHashMap;
import java.util.Map;

public record HttpRequest(String method, String path, Map<String, String> headers, String body) {
    public HttpRequest {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        headers = Map.copyOf(new LinkedHashMap<>(headers == null ? Map.of() : headers));
        body = body == null ? "" : body;
    }

    public String header(String name) {
        return headers.getOrDefault(name, "");
    }
}
