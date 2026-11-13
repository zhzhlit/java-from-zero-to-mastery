package io.github.javamastery.exercises.enterprisereview;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCourseRepository implements CourseRepository {
    private static final RowMapper<CourseDto> COURSE_MAPPER = (rs, rowNum) ->
            new CourseDto(rs.getLong("id"), rs.getString("title"), rs.getInt("available_seats"));

    private final JdbcTemplate jdbc;

    public JdbcCourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CourseDto> findAll() {
        return jdbc.query("SELECT id, title, available_seats FROM courses ORDER BY id", COURSE_MAPPER);
    }

    @Override
    public Optional<CourseDto> findById(long id) {
        return jdbc.query("SELECT id, title, available_seats FROM courses WHERE id = ?", COURSE_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public boolean reserveSeat(long id) {
        return jdbc.update("UPDATE courses SET available_seats = available_seats - 1 "
                + "WHERE id = ? AND available_seats > 0", id) == 1;
    }

    @Override
    public void insertEnrollment(long courseId, String student) {
        jdbc.update("INSERT INTO enrollments (course_id, student) VALUES (?, ?)", courseId, student);
    }

    @Override
    public List<EnrollmentDto> findEnrollmentsByStudent(String student) {
        return jdbc.query("SELECT course_id, student FROM enrollments WHERE student = ? ORDER BY course_id",
                (rs, rowNum) -> new EnrollmentDto(rs.getLong("course_id"), rs.getString("student")), student);
    }

    @Override
    public List<EnrollmentDto> findAllEnrollments() {
        return jdbc.query("SELECT course_id, student FROM enrollments ORDER BY course_id, student",
                (rs, rowNum) -> new EnrollmentDto(rs.getLong("course_id"), rs.getString("student")));
    }
}
