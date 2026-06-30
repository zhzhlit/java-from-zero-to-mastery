package io.github.javamastery.exercises.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("HttpRequestParser")
class HttpRequestParserTest {
    private final HttpRequestParser parser = new HttpRequestParser();

    @Test
    @DisplayName("解析请求行、请求头和正文")
    void parsesRequestLineHeadersAndBody() {
        HttpRequest request = parser.parse("""
                POST /courses HTTP/1.1\r
                Host: example.com\r
                Content-Type: application/json\r
                \r
                {"title":"Java"}""");

        assertEquals("POST", request.method());
        assertEquals("/courses", request.path());
        assertEquals("example.com", request.header("Host"));
        assertEquals("application/json", request.header("Content-Type"));
        assertEquals("{\"title\":\"Java\"}", request.body());
    }

    @Test
    @DisplayName("拒绝非 HTTP/1.1 请求行")
    void rejectsInvalidRequestLine() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("GET /courses HTTP/2"));
    }
}
