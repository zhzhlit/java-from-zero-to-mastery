package io.github.javamastery.exercises.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponseWriter {
    public String write(HttpResponse response) {
        Map<String, String> headers = new LinkedHashMap<>(response.headers());
        headers.putIfAbsent("Content-Length", String.valueOf(response.body().getBytes(StandardCharsets.UTF_8).length));

        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ")
                .append(response.statusCode())
                .append(' ')
                .append(response.reasonPhrase())
                .append("\r\n");
        headers.forEach((name, value) -> builder.append(name).append(": ").append(value).append("\r\n"));
        return builder.append("\r\n").append(response.body()).toString();
    }
}
