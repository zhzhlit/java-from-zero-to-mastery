package io.github.javamastery.exercises.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SimpleRouter")
class SimpleRouterTest {
    @Test
    @DisplayName("按方法和路径分派请求")
    void dispatchesByMethodAndPath() {
        SimpleRouter router = new SimpleRouter()
                .get("/health", request -> HttpResponse.ok("UP"))
                .post("/courses", request -> new HttpResponse(201, "Created", Map.of(), request.body()));

        assertEquals("UP", router.handle(new HttpRequest("GET", "/health", Map.of(), "")).body());
        assertEquals("Java", router.handle(new HttpRequest("POST", "/courses", Map.of(), "Java")).body());
    }

    @Test
    @DisplayName("找不到路由时返回 404")
    void returnsNotFoundForMissingRoute() {
        HttpResponse response = new SimpleRouter().handle(new HttpRequest("GET", "/missing", Map.of(), ""));

        assertEquals(404, response.statusCode());
        assertEquals("No route for GET /missing", response.body());
    }

    @Test
    @DisplayName("路由方法大小写不敏感")
    void normalizesRouteMethod() {
        SimpleRouter router = new SimpleRouter().get("/health", request -> HttpResponse.ok("UP"));

        assertEquals(200, router.handle(new HttpRequest("get", "/health", Map.of(), "")).statusCode());
    }
}
