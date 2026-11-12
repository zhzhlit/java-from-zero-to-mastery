package io.github.javamastery.exercises.springbootjdbc;

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
        throw new UnsupportedOperationException("练习：使用 JdbcTemplate 查询课程并按 id 排序");
    }

    @Override
    public Optional<CourseDto> findById(long id) {
        throw new UnsupportedOperationException("练习：使用参数化查询返回指定课程");
    }

    @Override
    public boolean reserveSeat(long id) {
        throw new UnsupportedOperationException("练习：通过条件 UPDATE 原子扣减名额");
    }

    @Override
    public void insertEnrollment(long courseId, String student) {
        throw new UnsupportedOperationException("练习：使用参数化 INSERT 写入报名记录");
    }
}
