package io.github.javamastery.exercises.http;

import java.util.LinkedHashMap;
import java.util.Map;

public record HttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, String body) {
    public HttpResponse {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("status code must be between 100 and 599");
        }
        if (reasonPhrase == null || reasonPhrase.isBlank()) {
            throw new IllegalArgumentException("reason phrase must not be blank");
        }
        headers = Map.copyOf(new LinkedHashMap<>(headers == null ? Map.of() : headers));
        body = body == null ? "" : body;
    }

    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "OK", Map.of("Content-Type", "text/plain; charset=utf-8"), body);
    }

    public static HttpResponse notFound(String body) {
        return new HttpResponse(404, "Not Found", Map.of("Content-Type", "text/plain; charset=utf-8"), body);
    }
}
