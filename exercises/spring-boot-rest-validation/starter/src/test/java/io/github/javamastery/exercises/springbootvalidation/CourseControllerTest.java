package io.github.javamastery.exercises.springbootvalidation;

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
            .standaloneSetup(new CourseController(new CourseService()))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    @Disabled("练习：移除 @Disabled，验证课程列表接口")
    @DisplayName("GET /api/courses 返回已发布课程")
    void listsPublishedCourses() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot 基础"))
                .andExpect(jsonPath("$[1].title").value("Spring Boot 配置"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，验证 404 场景")
    @DisplayName("GET /api/courses/{id} 找不到时返回 404")
    void returnsNotFoundForMissingCourse() throws Exception {
        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled("练习：移除 @Disabled，启用 @Valid 后验证合法创建")
    @DisplayName("POST /api/courses 创建合法课程")
    void createsCourseWhenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spring Boot 测试","lessonCount":12,"level":"intermediate"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/courses/4"))
                .andExpect(jsonPath("$.title").value("Spring Boot 测试"))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    @Disabled("练习：移除 @Disabled，为请求 DTO 添加 Bean Validation 规则并返回字段错误")
    @DisplayName("POST /api/courses 校验失败返回稳定错误响应")
    void returnsBadRequestWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","lessonCount":0,"level":"expert"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("请求参数校验失败"))
                .andExpect(jsonPath("$.errors[0].field").value("lessonCount"))
                .andExpect(jsonPath("$.errors[1].field").value("level"))
                .andExpect(jsonPath("$.errors[2].field").value("title"));
    }
}
