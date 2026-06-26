package io.github.javamastery.flagship.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

class StudyProgressTest {

    @Test
    void recordsCompletedLessonsAndBuildsSummary() {
        Course course = SampleCourses.javaCoreReviewCourse();
        StudyProgress progress = new StudyProgress(
                course.code(),
                LocalDateTime.of(2026, 6, 26, 9, 0),
                null,
                Set.of());

        progress.completeLesson(course, "core-oop", LocalDateTime.of(2026, 6, 26, 10, 30));

        assertTrue(progress.completed("CORE-OOP"));
        assertEquals(33, progress.completionPercent(course));
        assertEquals("Java 核心综合复盘 | 1/3 | 33% | last 2026-06-26 10:30", progress.summary(course));
    }

    @Test
    void doesNotCountDuplicateLessonTwice() {
        Course course = SampleCourses.javaCoreReviewCourse();
        StudyProgress progress = new StudyProgress(
                course.code(),
                LocalDateTime.of(2026, 6, 26, 9, 0),
                null,
                Set.of());

        progress.completeLesson(course, "core-oop", LocalDateTime.of(2026, 6, 26, 10, 0));
        progress.completeLesson(course, "core-oop", LocalDateTime.of(2026, 6, 26, 11, 0));

        assertEquals(1, progress.completedLessonIds().size());
        assertEquals(33, progress.completionPercent(course));
    }

    @Test
    void rejectsLessonOutsideCourse() {
        Course course = SampleCourses.javaCoreReviewCourse();
        StudyProgress progress = new StudyProgress(
                course.code(),
                LocalDateTime.of(2026, 6, 26, 9, 0),
                null,
                Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> progress.completeLesson(course, "missing", LocalDateTime.of(2026, 6, 26, 10, 0)));
    }
}
