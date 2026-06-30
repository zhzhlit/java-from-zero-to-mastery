package io.github.javamastery.exercises.servlet;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebResponse {
    private int status = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final StringBuilder body = new StringBuilder();

    public int status() {
        return status;
    }

    public WebResponse status(int status) {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be between 100 and 599");
        }
        this.status = status;
        return this;
    }

    public WebResponse header(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("header name must not be blank");
        }
        headers.put(name, value == null ? "" : value);
        return this;
    }

    public WebResponse write(String text) {
        body.append(text == null ? "" : text);
        return this;
    }

    public Map<String, String> headers() {
        return Map.copyOf(headers);
    }

    public String body() {
        return body.toString();
    }
}
