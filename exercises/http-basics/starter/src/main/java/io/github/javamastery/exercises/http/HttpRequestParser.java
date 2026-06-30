package io.github.javamastery.exercises.http;

import java.util.Map;

public class HttpRequestParser {
    public HttpRequest parse(String rawRequest) {
        return new HttpRequest("GET", "/", Map.of(), "");
    }
}
