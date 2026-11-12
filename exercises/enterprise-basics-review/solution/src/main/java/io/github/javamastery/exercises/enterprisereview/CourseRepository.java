package io.github.javamastery.exercises.enterprisereview;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    List<CourseDto> findAll();

    Optional<CourseDto> findById(long id);

    boolean reserveSeat(long id);

    void insertEnrollment(long courseId, String student);

    List<EnrollmentDto> findEnrollmentsByStudent(String student);

    List<EnrollmentDto> findAllEnrollments();
}
