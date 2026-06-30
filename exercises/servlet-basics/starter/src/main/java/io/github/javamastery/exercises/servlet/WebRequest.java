package io.github.javamastery.exercises.servlet;

import java.util.Map;

public record WebRequest(String method, String path, Map<String, String> parameters, Map<String, String> headers, String body) {
    public WebRequest {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? "" : body;
    }

    public String parameter(String name) {
        return "";
    }

    public String header(String name) {
        return "";
    }
}
