package io.github.javamastery.exercises.webreview;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiResponse {
    private int status = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String body = "";

    public int status() {
        return status;
    }

    public ApiResponse status(int status) {
        this.status = status;
        return this;
    }

    public ApiResponse header(String name, String value) {
        headers.put(name, value == null ? "" : value);
        return this;
    }

    public ApiResponse body(String body) {
        this.body = body == null ? "" : body;
        return this;
    }

    public Map<String, String> headers() {
        return Map.copyOf(headers);
    }

    public String body() {
        return body;
    }
}
