package io.github.javamastery.exercises.servlet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("WebRequest 与 WebResponse")
class WebRequestResponseTest {
    @Test
    @DisplayName("请求能读取参数、头部和正文")
    void requestReadsParametersHeadersAndBody() {
        WebRequest request = new WebRequest(
                "GET",
                "/courses",
                Map.of("page", "2"),
                Map.of("Accept", "application/json"),
                ""
        );

        assertEquals("2", request.parameter("page"));
        assertEquals("application/json", request.header("Accept"));
        assertEquals("", request.parameter("missing"));
    }

    @Test
    @DisplayName("响应能设置状态、头部并追加正文")
    void responseSetsStatusHeadersAndBody() {
        WebResponse response = new WebResponse()
                .status(201)
                .header("Location", "/courses/1")
                .write("created")
                .write("!");

        assertEquals(201, response.status());
        assertEquals("/courses/1", response.headers().get("Location"));
        assertEquals("created!", response.body());
    }

    @Test
    @DisplayName("请求路径必须以斜杠开头")
    void requestPathMustStartWithSlash() {
        assertThrows(IllegalArgumentException.class, () -> new WebRequest("GET", "courses", Map.of(), Map.of(), ""));
    }
}
