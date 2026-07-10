package io.github.javamastery.exercises.springbootlayers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CourseController")
class CourseControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CourseController(new CourseService(new InMemoryCourseRepository())))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    @Disabled("练习：移除 @Disabled，验证 Controller 只负责 HTTP 响应形状")
    @DisplayName("GET /api/courses 返回所有课程")
    void listsCourses() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot 基础"))
                .andExpect(jsonPath("$[2].status").value("DRAFT"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，验证创建接口和 Location 响应头")
    @DisplayName("POST /api/courses 创建草稿课程")
    void createsDraftCourse() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spring Boot 分层架构","lessonCount":7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/courses/4"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，验证发布接口调用 Service 流程")
    @DisplayName("POST /api/courses/{id}/publish 发布课程")
    void publishesCourse() throws Exception {
        mockMvc.perform(post("/api/courses/3/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，业务规则失败应由统一异常处理返回 409")
    @DisplayName("业务规则失败返回 409")
    void returnsConflictForBusinessRuleViolation() throws Exception {
        mockMvc.perform(post("/api/courses/1/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }
}
