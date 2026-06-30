package io.github.javamastery.exercises.webreview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CourseWebApplication")
class CourseWebApplicationTest {
    @Test
    @DisplayName("按 HTTP 方法和路径分派到课程接口")
    void dispatchesByMethodAndPath() {
        CourseWebApplication app = app();

        ApiResponse response = app.handle(new ApiRequest("GET", "/courses", Map.of(), Map.of()));

        assertEquals(200, response.status());
        assertEquals("1:JDBC", response.body());
    }

    @Test
    @DisplayName("写操作缺少 Bearer token 时返回 401")
    void protectsWriteOperations() {
        CourseWebApplication app = app();

        ApiResponse response = app.handle(new ApiRequest("POST", "/courses", Map.of(), Map.of("title", "Servlet")));

        assertEquals(401, response.status());
        assertEquals("Bearer", response.headers().get("WWW-Authenticate"));
    }

    @Test
    @DisplayName("带 token 的写操作可以创建课程")
    void allowsAuthorizedWrites() {
        CourseWebApplication app = app();

        ApiResponse response = app.handle(new ApiRequest(
                "POST",
                "/courses",
                Map.of("Authorization", "Bearer secret"),
                Map.of("title", "Servlet", "lessonCount", "7")
        ));

        assertEquals(201, response.status());
        assertEquals("2:Servlet", response.body());
    }

    @Test
    @DisplayName("课程路径不支持的方法返回 405")
    void returnsMethodNotAllowedForCoursePath() {
        CourseWebApplication app = app();

        ApiResponse response = app.handle(new ApiRequest("DELETE", "/courses", Map.of(), Map.of()));

        assertEquals(405, response.status());
        assertEquals("GET, POST", response.headers().get("Allow"));
    }

    private CourseWebApplication app() {
        InMemoryCourseStore store = new InMemoryCourseStore(List.of(new CourseSummary(1, "JDBC", 8, true)));
        return new CourseWebApplication(store, "secret");
    }
}
