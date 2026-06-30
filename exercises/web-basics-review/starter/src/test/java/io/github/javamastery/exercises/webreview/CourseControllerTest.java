package io.github.javamastery.exercises.webreview;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CourseController")
class CourseControllerTest {
    private final InMemoryCourseStore store = new InMemoryCourseStore(List.of(
            new CourseSummary(1, "JDBC", 8, true),
            new CourseSummary(2, "Draft", 3, false)
    ));
    private final CourseController controller = new CourseController(store);

    @Test
    @Disabled("练习：移除 @Disabled，完成对应实现")
    @DisplayName("列表接口只返回发布课程")
    void listsPublishedCourses() {
        ApiResponse response = controller.listPublished(new ApiRequest("GET", "/courses", Map.of(), Map.of()));

        assertEquals(200, response.status());
        assertEquals("1:JDBC", response.body());
        assertEquals("text/plain; charset=utf-8", response.headers().get("Content-Type"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，完成对应实现")
    @DisplayName("详情接口找不到课程时返回 404")
    void returnsNotFoundForMissingCourse() {
        ApiResponse response = controller.show(new ApiRequest("GET", "/courses/detail", Map.of(), Map.of("id", "99")));

        assertEquals(404, response.status());
        assertEquals("course not found", response.body());
    }

    @Test
    @Disabled("练习：移除 @Disabled，完成对应实现")
    @DisplayName("创建接口校验标题并返回 Location")
    void createsCourseWithLocation() {
        ApiResponse response = controller.create(new ApiRequest("POST", "/courses", Map.of(), Map.of("title", "Servlet", "lessonCount", "7")));

        assertEquals(201, response.status());
        assertEquals("/courses/3", response.headers().get("Location"));
        assertEquals("3:Servlet", response.body());
    }
}
