package io.github.javamastery.exercises.http;

import java.util.Map;

public record HttpRequest(String method, String path, Map<String, String> headers, String body) {
    public HttpRequest {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? "" : body;
    }

    public String header(String name) {
        return "";
    }
}
