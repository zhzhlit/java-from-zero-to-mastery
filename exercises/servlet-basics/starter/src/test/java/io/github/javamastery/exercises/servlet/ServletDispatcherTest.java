package io.github.javamastery.exercises.servlet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ServletDispatcher")
class ServletDispatcherTest {
    @Test
    @Disabled("练习：移除 @Disabled，按路径找到 servlet 并处理请求")
    @DisplayName("按路径找到 servlet 并处理请求")
    void dispatchesToServletByPath() {
        ServletDispatcher dispatcher = new ServletDispatcher()
                .register("/health", new MiniServlet() {
                    @Override
                    protected void doGet(WebRequest request, WebResponse response) {
                        response.write("UP");
                    }
                });

        WebResponse response = dispatcher.dispatch(new WebRequest("GET", "/health", Map.of(), Map.of(), ""));

        assertEquals(200, response.status());
        assertEquals("UP", response.body());
    }

    @Test
    @Disabled("练习：移除 @Disabled，过滤器按注册顺序包裹 servlet")
    @DisplayName("过滤器按注册顺序包裹 servlet")
    void filtersWrapServletInOrder() {
        ServletDispatcher dispatcher = new ServletDispatcher()
                .addFilter((request, response, chain) -> {
                    response.header("X-First", "before");
                    chain.doFilter(request, response);
                    response.write("-after-first");
                })
                .addFilter((request, response, chain) -> {
                    response.header("X-Second", request.header("Trace"));
                    chain.doFilter(request, response);
                })
                .register("/courses", new MiniServlet() {
                    @Override
                    protected void doGet(WebRequest request, WebResponse response) {
                        response.write("courses");
                    }
                });

        WebResponse response = dispatcher.dispatch(new WebRequest("GET", "/courses", Map.of(), Map.of("Trace", "on"), ""));

        assertEquals("before", response.headers().get("X-First"));
        assertEquals("on", response.headers().get("X-Second"));
        assertEquals("courses-after-first", response.body());
    }

    @Test
    @Disabled("练习：移除 @Disabled，缺失 servlet 返回 404")
    @DisplayName("缺失 servlet 返回 404")
    void missingServletReturnsNotFound() {
        WebResponse response = new ServletDispatcher().dispatch(new WebRequest("GET", "/missing", Map.of(), Map.of(), ""));

        assertEquals(404, response.status());
        assertEquals("No servlet for /missing", response.body());
    }
}
