package io.github.javamastery.exercises.servlet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MiniServlet")
class MiniServletTest {
    @Test
    @DisplayName("service 按 HTTP 方法调用 doGet 或 doPost")
    void serviceDispatchesToMethodHandlers() {
        MiniServlet servlet = new MiniServlet() {
            @Override
            protected void doGet(WebRequest request, WebResponse response) {
                response.write("list");
            }

            @Override
            protected void doPost(WebRequest request, WebResponse response) {
                response.status(201).write(request.body());
            }
        };

        WebResponse getResponse = new WebResponse();
        servlet.service(new WebRequest("GET", "/courses", Map.of(), Map.of(), ""), getResponse);
        assertEquals("list", getResponse.body());

        WebResponse postResponse = new WebResponse();
        servlet.service(new WebRequest("POST", "/courses", Map.of(), Map.of(), "Java"), postResponse);
        assertEquals(201, postResponse.status());
        assertEquals("Java", postResponse.body());
    }

    @Test
    @DisplayName("未覆盖的方法返回 405")
    void defaultMethodReturnsMethodNotAllowed() {
        MiniServlet servlet = new MiniServlet() {
        };
        WebResponse response = new WebResponse();

        servlet.service(new WebRequest("DELETE", "/courses/1", Map.of(), Map.of(), ""), response);

        assertEquals(405, response.status());
        assertEquals("GET, POST", response.headers().get("Allow"));
        assertEquals("Method Not Allowed", response.body());
    }
}
