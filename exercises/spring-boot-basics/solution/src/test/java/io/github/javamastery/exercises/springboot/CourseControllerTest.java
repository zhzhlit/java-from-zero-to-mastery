package io.github.javamastery.exercises.springboot;

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
            .build();

    @Test
    @DisplayName("GET /api/courses 返回已发布课程")
    void listsPublishedCourses() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("JDBC 基础"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} 找不到时返回 404")
    void returnsNotFoundForMissingCourse() throws Exception {
        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/courses 创建课程并返回 Location")
    void createsCourseWithLocation() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spring Boot 基础","lessonCount":9}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/courses/4"))
                .andExpect(jsonPath("$.published").value(false));
    }
}
