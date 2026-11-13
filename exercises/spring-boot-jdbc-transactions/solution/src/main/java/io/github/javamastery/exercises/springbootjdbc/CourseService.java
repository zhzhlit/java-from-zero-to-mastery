package io.github.javamastery.exercises.springbootjdbc;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class CourseService {
    private final CourseRepository courses;

    public CourseService(CourseRepository courses) {
        this.courses = courses;
    }

    public List<CourseDto> listCourses() {
        return courses.findAll();
    }

    @Transactional
    public EnrollmentDto enroll(long courseId, String student) {
        if (student == null || student.isBlank() || student.trim().length() > 100) {
            throw new IllegalArgumentException("学生标识必须为 1 到 100 个字符");
        }
        String normalized = student.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("规范化后的学生标识不能超过 100 个字符");
        }
        courses.findById(courseId).orElseThrow(() -> new NoSuchElementException("课程不存在"));
        if (!courses.reserveSeat(courseId)) {
            throw new IllegalStateException("课程名额已满");
        }
        courses.insertEnrollment(courseId, normalized);
        return new EnrollmentDto(courseId, normalized);
    }
}
