package io.github.javamastery.exercises.springbootjdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled("练习：完成 JdbcCourseRepository 和 CourseService，添加事务边界后移除 @Disabled")
class CourseEnrollmentTest {
    @Autowired
    private CourseService service;
    @Autowired
    private CourseRepository courses;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM enrollments");
        jdbc.update("UPDATE courses SET available_seats = 2 WHERE id = 1");
        jdbc.update("UPDATE courses SET available_seats = 0 WHERE id = 2");
    }

    @Test
    void queriesDatabaseThroughHttp() throws Exception {
        mvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].availableSeats").value(2))
                .andExpect(jsonPath("$[1].availableSeats").value(0));
    }

    @Test
    void commitsSeatAndEnrollmentTogether() throws Exception {
        mvc.perform(post("/courses/1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"student\":\"  ALICE  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.student").value("alice"));

        assertEquals(1, seats(1));
        assertEquals(1, enrollmentCount());
        assertEquals("alice", jdbc.queryForObject("SELECT student FROM enrollments", String.class));
    }

    @Test
    void rollsBackSeatWhenSecondWriteViolatesUniqueConstraint() {
        service.enroll(1, "alice");

        assertThrows(DuplicateKeyException.class, () -> service.enroll(1, " ALICE "));

        assertEquals(1, seats(1));
        assertEquals(1, enrollmentCount());
    }

    @Test
    void mapsDuplicateToConflictAfterRollback() throws Exception {
        service.enroll(1, "alice");

        mvc.perform(post("/courses/1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"student\":\"alice\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("不能重复报名同一课程"));

        assertEquals(1, seats(1));
        assertEquals(1, enrollmentCount());
    }

    @Test
    void rejectsFullCourseWithoutWriting() throws Exception {
        mvc.perform(post("/courses/2/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"student\":\"alice\"}"))
                .andExpect(status().isConflict());

        assertEquals(0, seats(2));
        assertEquals(0, enrollmentCount());
    }

    @Test
    void cannotReserveBeyondCapacity() {
        service.enroll(1, "alice");
        service.enroll(1, "bob");

        assertThrows(IllegalStateException.class, () -> service.enroll(1, "charlie"));

        assertEquals(0, seats(1));
        assertEquals(2, enrollmentCount());
    }

    @Test
    void rejectsMissingCourse() throws Exception {
        assertThrows(NoSuchElementException.class, () -> service.enroll(999, "alice"));
        mvc.perform(post("/courses/999/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"student\":\"alice\"}"))
                .andExpect(status().isNotFound());
        assertEquals(0, enrollmentCount());
    }

    @Test
    void rejectsInvalidStudentBeforeWriting() throws Exception {
        for (String student : new String[]{null, "  ", "a".repeat(101)}) {
            assertThrows(IllegalArgumentException.class, () -> service.enroll(1, student));
        }
        mvc.perform(post("/courses/1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        assertEquals(2, seats(1));
        assertEquals(0, enrollmentCount());
    }

    @Test
    void bindsStudentAsSqlParameter() {
        String student = "o'hara'); delete from courses; --";
        service.enroll(1, student);

        assertEquals(student, jdbc.queryForObject("SELECT student FROM enrollments", String.class));
        assertEquals(2, courses.findAll().size());
        assertEquals(1, seats(1));
    }

    private int seats(long courseId) {
        return courses.findById(courseId).orElseThrow().availableSeats();
    }

    private int enrollmentCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM enrollments", Integer.class);
    }
}
