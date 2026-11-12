package io.github.javamastery.exercises.enterprisereview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        if (student == null || student.isBlank() || student.length() > 100) {
            throw new IllegalArgumentException("学生标识必须为 1 到 100 个字符");
        }
        courses.findById(courseId).orElseThrow(() -> new NoSuchElementException("课程不存在"));
        if (!courses.reserveSeat(courseId)) {
            throw new IllegalStateException("课程名额已满");
        }
        courses.insertEnrollment(courseId, student);
        return new EnrollmentDto(courseId, student);
    }

    public List<EnrollmentDto> enrollmentsFor(String student) {
        return courses.findEnrollmentsByStudent(student);
    }

    public List<EnrollmentDto> allEnrollments() {
        return courses.findAllEnrollments();
    }
}
