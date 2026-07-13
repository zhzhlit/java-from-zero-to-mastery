package io.github.javamastery.exercises.springbootlayers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CourseService")
class CourseServiceTest {
    private final CourseService service = new CourseService(new InMemoryCourseRepository());

    @Test
    @DisplayName("创建课程时保存为草稿并规范化标题")
    void createsDraftCourse() {
        CourseDto course = service.create(new CreateCourseRequest("  Spring Boot 分层架构  ", 7));

        assertEquals(4, course.id());
        assertEquals("Spring Boot 分层架构", course.title());
        assertEquals("DRAFT", course.status());
    }

    @Test
    @DisplayName("课程标题不能重复")
    void rejectsDuplicateTitle() {
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> service.create(new CreateCourseRequest("spring boot 基础", 6)));

        assertEquals("课程标题不能重复", exception.getMessage());
    }

    @Test
    @DisplayName("发布草稿课程")
    void publishesDraftCourse() {
        CourseDto course = service.publish(3);

        assertEquals("PUBLISHED", course.status());
    }

    @Test
    @DisplayName("不能重复发布课程")
    void rejectsPublishingAlreadyPublishedCourse() {
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.publish(1));

        assertEquals("课程已经发布", exception.getMessage());
    }
}
