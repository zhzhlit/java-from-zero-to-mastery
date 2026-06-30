package io.github.javamastery.exercises.webreview;

import java.util.Map;

public class ApiResponse {
    public int status() {
        return 200;
    }

    public ApiResponse status(int status) {
        return this;
    }

    public ApiResponse header(String name, String value) {
        return this;
    }

    public ApiResponse body(String body) {
        return this;
    }

    public Map<String, String> headers() {
        return Map.of();
    }

    public String body() {
        return "";
    }
}
