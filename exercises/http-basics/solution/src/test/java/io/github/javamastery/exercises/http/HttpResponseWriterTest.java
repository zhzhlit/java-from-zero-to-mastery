package io.github.javamastery.exercises.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HttpResponseWriter")
class HttpResponseWriterTest {
    private final HttpResponseWriter writer = new HttpResponseWriter();

    @Test
    @DisplayName("写出状态行、响应头、空行和正文")
    void writesStatusLineHeadersBlankLineAndBody() {
        String raw = writer.write(new HttpResponse(201, "Created", Map.of("Location", "/courses/1"), "created"));

        assertTrue(raw.startsWith("HTTP/1.1 201 Created\r\n"));
        assertTrue(raw.contains("Location: /courses/1\r\n"));
        assertTrue(raw.contains("Content-Length: 7\r\n"));
        assertTrue(raw.endsWith("\r\n\r\ncreated"));
    }

    @Test
    @DisplayName("Content-Length 使用 UTF-8 字节长度")
    void contentLengthUsesUtf8Bytes() {
        String raw = writer.write(HttpResponse.ok("你好"));

        assertTrue(raw.contains("Content-Length: 6\r\n"));
    }
}
