package io.github.javamastery.exercises.servlet;

import java.util.LinkedHashMap;
import java.util.Map;

public record WebRequest(String method, String path, Map<String, String> parameters, Map<String, String> headers, String body) {
    public WebRequest {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        parameters = Map.copyOf(new LinkedHashMap<>(parameters == null ? Map.of() : parameters));
        headers = Map.copyOf(new LinkedHashMap<>(headers == null ? Map.of() : headers));
        body = body == null ? "" : body;
    }

    public String parameter(String name) {
        return parameters.getOrDefault(name, "");
    }

    public String header(String name) {
        return headers.getOrDefault(name, "");
    }
}
