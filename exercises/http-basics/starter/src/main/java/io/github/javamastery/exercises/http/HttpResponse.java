package io.github.javamastery.exercises.http;

import java.util.Map;

public record HttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, String body) {
    public HttpResponse {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? "" : body;
    }

    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "OK", Map.of(), body);
    }

    public static HttpResponse notFound(String body) {
        return new HttpResponse(404, "Not Found", Map.of(), body);
    }
}
