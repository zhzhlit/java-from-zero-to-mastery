package io.github.javamastery.exercises.enterprisereview;

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
        throw new UnsupportedOperationException("练习：以事务包住名额扣减和报名写入，保留输入校验与异常传播");
    }

    public List<EnrollmentDto> enrollmentsFor(String student) {
        return courses.findEnrollmentsByStudent(student);
    }

    public List<EnrollmentDto> allEnrollments() {
        return courses.findAllEnrollments();
    }
}
