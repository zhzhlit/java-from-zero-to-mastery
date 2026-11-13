package io.github.javamastery.exercises.springbootjdbc;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseRepository courses;

    public CourseService(CourseRepository courses) {
        this.courses = courses;
    }

    public List<CourseDto> listCourses() {
        return courses.findAll();
    }

    public EnrollmentDto enroll(long courseId, String student) {
        throw new UnsupportedOperationException("练习：校验输入，并在同一个事务中扣减名额和写入报名记录");
    }
}
