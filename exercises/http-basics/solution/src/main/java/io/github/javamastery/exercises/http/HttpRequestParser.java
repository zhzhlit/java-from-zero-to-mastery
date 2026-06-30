package io.github.javamastery.exercises.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequestParser {
    public HttpRequest parse(String rawRequest) {
        if (rawRequest == null || rawRequest.isBlank()) {
            throw new IllegalArgumentException("request must not be blank");
        }

        String normalized = rawRequest.replace("\r\n", "\n");
        String[] sections = normalized.split("\n\n", 2);
        String[] lines = sections[0].split("\n");
        String[] requestLine = lines[0].split(" ");
        if (requestLine.length != 3 || !"HTTP/1.1".equals(requestLine[2])) {
            throw new IllegalArgumentException("request line must be: METHOD /path HTTP/1.1");
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (int index = 1; index < lines.length; index++) {
            String line = lines[index];
            if (line.isBlank()) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new IllegalArgumentException("invalid header line: " + line);
            }
            headers.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
        }

        String body = sections.length == 2 ? sections[1] : "";
        return new HttpRequest(requestLine[0], requestLine[1], headers, body);
    }
}
