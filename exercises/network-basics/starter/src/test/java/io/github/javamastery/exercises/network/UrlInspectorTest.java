package io.github.javamastery.exercises.network;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UrlInspector")
class UrlInspectorTest {
    private final UrlInspector inspector = new UrlInspector();

    @Test
    @Disabled("练习：移除 @Disabled，解析 HTTPS URL 并补默认端口")
    @DisplayName("解析 HTTPS URL 并补默认端口")
    void parsesHttpsUrlWithDefaultPort() {
        UrlParts parts = inspector.parse("https://Example.com/courses/java?level=beginner&sort=asc");

        assertEquals("https", parts.scheme());
        assertEquals("example.com", parts.host());
        assertEquals(443, parts.port());
        assertEquals("/courses/java", parts.path());
        assertEquals("level=beginner&sort=asc", parts.query());
    }

    @Test
    @Disabled("练习：移除 @Disabled，保留显式端口并补根路径")
    @DisplayName("保留显式端口并为空路径补斜杠")
    void keepsExplicitPortAndNormalizesEmptyPath() {
        UrlParts parts = inspector.parse("http://localhost:8080");

        assertEquals("http", parts.scheme());
        assertEquals("localhost", parts.host());
        assertEquals(8080, parts.port());
        assertEquals("/", parts.path());
        assertEquals("", parts.query());
    }

    @Test
    @Disabled("练习：移除 @Disabled，拼出路径和查询串")
    @DisplayName("拼出 HTTP 请求目标路径")
    void buildsPathWithQuery() {
        assertEquals("/search?q=java%2021", inspector.pathWithQuery("https://example.com/search?q=java%2021"));
        assertEquals("/", inspector.pathWithQuery("https://example.com"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，校验 URL 结构")
    @DisplayName("缺少协议或主机时抛出异常")
    void rejectsInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> inspector.parse("not-a-url"));
    }
}
