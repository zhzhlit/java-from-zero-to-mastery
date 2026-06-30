package io.github.javamastery.exercises.springboot;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CourseService")
class CourseServiceTest {
    private final CourseService service = new CourseService();

    @Test
    @Disabled("练习：移除 @Disabled，只返回已发布课程")
    @DisplayName("只返回已发布课程")
    void listsPublishedCoursesOnly() {
        assertEquals(2, service.publishedCourses().size());
        assertEquals("JDBC 基础", service.publishedCourses().getFirst().title());
    }

    @Test
    @Disabled("练习：移除 @Disabled，创建草稿课程")
    @DisplayName("创建课程时生成草稿课程")
    void createsDraftCourse() {
        CourseDto created = service.create(new CreateCourseRequest("Spring Boot 基础", 9));

        assertEquals(4, created.id());
        assertEquals("Spring Boot 基础", created.title());
        assertFalse(created.published());
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝空标题")
    @DisplayName("创建课程时拒绝空标题")
    void rejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new CreateCourseRequest(" ", 1)));
    }
}
