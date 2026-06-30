package io.github.javamastery.exercises.servlet;

import java.util.Map;

public class WebResponse {
    public int status() {
        return 200;
    }

    public WebResponse status(int status) {
        return this;
    }

    public WebResponse header(String name, String value) {
        return this;
    }

    public WebResponse write(String text) {
        return this;
    }

    public Map<String, String> headers() {
        return Map.of();
    }

    public String body() {
        return "";
    }
}
