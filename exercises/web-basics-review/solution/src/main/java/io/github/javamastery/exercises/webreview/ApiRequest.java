package io.github.javamastery.exercises.webreview;

import java.util.Map;

public record ApiRequest(String method, String path, Map<String, String> headers, Map<String, String> form) {
    public ApiRequest {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        form = form == null ? Map.of() : Map.copyOf(form);
    }

    public String header(String name) {
        return headers.getOrDefault(name, "");
    }

    public String formValue(String name) {
        return form.getOrDefault(name, "");
    }
}
