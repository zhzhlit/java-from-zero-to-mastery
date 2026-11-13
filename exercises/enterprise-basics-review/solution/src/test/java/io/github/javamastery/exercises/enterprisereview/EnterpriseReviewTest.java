package io.github.javamastery.exercises.enterprisereview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "learning.security.student-password=student-test-password",
        "learning.security.admin-password=admin-test-password"
})
@AutoConfigureMockMvc
class EnterpriseReviewTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM enrollments");
        jdbc.update("UPDATE courses SET available_seats = 2 WHERE id = 1");
        jdbc.update("UPDATE courses SET available_seats = 0 WHERE id = 2");
    }

    @Test
    void servesPublicCourses() throws Exception {
        mvc.perform(get("/courses")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].availableSeats").value(2));
    }

    @Test
    void logsInAndEnrollsThroughRealSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("student").password("student-test-password"))
                .andExpect(authenticated()).andExpect(redirectedUrl("/profile"))
                .andReturn().getRequest().getSession(false);
        mvc.perform(post("/courses/1/enrollments").session(session).with(csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.student").value("student"));
        assertState(1, 1);
    }

    @Test
    void rejectsAnonymousWriteWithoutChangingDatabase() throws Exception {
        mvc.perform(post("/courses/1/enrollments").with(csrf())).andExpect(status().isFound());
        assertState(2, 0);
    }

    @Test
    void rejectsMissingCsrfAndStillReturnsRequestId() throws Exception {
        mvc.perform(post("/courses/1/enrollments").with(user("student").roles("STUDENT"))
                        .header("X-Request-Id", "denied-write"))
                .andExpect(status().isForbidden()).andExpect(header().string("X-Request-Id", "denied-write"));
        assertState(2, 0);
        assertNull(MDC.get("requestId"));
    }

    @Test
    void rejectsWrongRoleWithoutChangingDatabase() throws Exception {
        mvc.perform(post("/courses/1/enrollments").with(user("viewer").roles("VIEWER")).with(csrf()))
                .andExpect(status().isForbidden());
        assertState(2, 0);
    }

    @Test
    void ignoresClaimedStudentAndUsesPrincipal() throws Exception {
        mvc.perform(post("/courses/1/enrollments").with(user("student").roles("STUDENT")).with(csrf())
                        .contentType("application/json").content("{\"student\":\"victim\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.student").value("student"));
        assertEquals("student", jdbc.queryForObject("SELECT student FROM enrollments", String.class));
    }

    @Test
    void duplicateEnrollmentRollsBackSeat() throws Exception {
        for (int expected : new int[]{201, 409}) {
            mvc.perform(post("/courses/1/enrollments").with(user("student").roles("STUDENT")).with(csrf()))
                    .andExpect(status().is(expected));
        }
        assertState(1, 1);
    }

    @Test
    void rejectsFullCourse() throws Exception {
        mvc.perform(post("/courses/2/enrollments").with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().isConflict());
        assertState(2, 0);
    }

    @Test
    void rejectsMissingCourse() throws Exception {
        mvc.perform(post("/courses/999/enrollments").with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().isNotFound());
        assertState(2, 0);
    }

    @Test
    void studentCanOnlyReadOwnEnrollments() throws Exception {
        jdbc.update("INSERT INTO enrollments (course_id, student) VALUES (1, 'other')");
        mvc.perform(get("/enrollments").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/enrollments").with(user("other").roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].student").value("other"));
    }

    @Test
    void adminCanReadAllButStudentCannot() throws Exception {
        jdbc.update("INSERT INTO enrollments (course_id, student) VALUES (1, 'other')");
        mvc.perform(get("/admin/enrollments").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/enrollments").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].student").value("other"));
    }

    @Test
    void replacesUnsafeRequestIdAndCleansMdc() throws Exception {
        String id = mvc.perform(get("/courses").header("X-Request-Id", "bad id"))
                .andExpect(status().isOk()).andReturn().getResponse().getHeader("X-Request-Id");
        assertEquals(id, java.util.UUID.fromString(id).toString());
        assertNull(MDC.get("requestId"));
    }

    private void assertState(int seats, int enrollments) {
        assertEquals(seats, jdbc.queryForObject("SELECT available_seats FROM courses WHERE id = 1", Integer.class));
        assertEquals(enrollments, jdbc.queryForObject("SELECT COUNT(*) FROM enrollments", Integer.class));
    }
}
