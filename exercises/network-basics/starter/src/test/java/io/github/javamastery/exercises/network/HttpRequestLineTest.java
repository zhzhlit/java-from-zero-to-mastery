package io.github.javamastery.exercises.network;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("HttpRequestLine")
class HttpRequestLineTest {
    private final HttpRequestLine requestLine = new HttpRequestLine();

    @Test
    @Disabled("练习：移除 @Disabled，从 URL 构造请求行")
    @DisplayName("从 URL 构造 HTTP/1.1 请求行")
    void buildsRequestLineWithPathAndQuery() {
        String line = requestLine.build("get", "https://example.com/courses?page=2");

        assertEquals("GET /courses?page=2 HTTP/1.1", line);
    }

    @Test
    @Disabled("练习：移除 @Disabled，根路径 URL 使用斜杠")
    @DisplayName("根路径 URL 使用斜杠作为请求目标")
    void usesSlashForRootTarget() {
        assertEquals("HEAD / HTTP/1.1", requestLine.build("HEAD", "https://example.com"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝无效方法")
    @DisplayName("拒绝空方法和未支持的方法")
    void rejectsInvalidMethods() {
        assertThrows(IllegalArgumentException.class, () -> requestLine.build("", "https://example.com"));
        assertThrows(IllegalArgumentException.class, () -> requestLine.build("CONNECT", "https://example.com"));
    }
}
