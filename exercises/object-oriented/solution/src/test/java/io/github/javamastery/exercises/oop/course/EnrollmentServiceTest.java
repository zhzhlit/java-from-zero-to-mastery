package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnrollmentServiceTest {

    @Test
    void enrollsStudentsAndTracksRemainingSeats() {
        Lesson lesson = new Lesson("异常处理", 40);
        EnrollmentService service = new EnrollmentService(lesson, 2);

        service.enroll("  Ada  ");
        service.enroll("Linus");

        assertSame(lesson, service.getLesson());
        assertEquals(2, service.getCapacity());
        assertEquals(2, service.getEnrolledCount());
        assertEquals(0, service.remainingSeats());
    }

    @Test
    void throwsEnrollmentExceptionWhenFull() {
        EnrollmentService service = new EnrollmentService(
                new Lesson("异常处理", 40),
                1);
        service.enroll("Ada");

        EnrollmentException exception = assertThrows(EnrollmentException.class,
                () -> service.enroll("Grace"));

        assertEquals("course is full: 异常处理", exception.getMessage());
        assertEquals(1, service.getEnrolledCount());
    }

    @Test
    void tryEnrollConvertsExpectedExceptionsToMessages() {
        EnrollmentService service = new EnrollmentService(
                new Lesson("接口复盘", 30),
                1);

        assertEquals("Ada enrolled in 接口复盘", service.tryEnroll("  Ada  "));
        assertEquals("Enrollment failed: course is full: 接口复盘", service.tryEnroll("Grace"));
        assertEquals("Enrollment failed: student name must not be blank", service.tryEnroll(""));
        assertEquals(1, service.getEnrolledCount());
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> new EnrollmentService(null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new EnrollmentService(new Lesson("异常处理", 40), -1));

        EnrollmentService service = new EnrollmentService(new Lesson("异常处理", 40), 1);
        assertThrows(IllegalArgumentException.class,
                () -> service.enroll(null));
    }
}
