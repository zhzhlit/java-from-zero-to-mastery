package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseProgressTest {

    @Test
    void recordsStudyMinutesAgainstOneLesson() {
        Lesson lesson = new Lesson("封装", 40);
        CourseProgress progress = new CourseProgress("  Ada  ", lesson);

        progress.recordStudyMinutes(10);
        progress.recordStudyMinutes(15);

        assertEquals("Ada", progress.getStudentName());
        assertSame(lesson, progress.getLesson());
        assertEquals(25, progress.getCompletedMinutes());
        assertEquals(15, progress.remainingMinutes());
        assertEquals(0.625, progress.completionRate(), 0.0001);
        assertFalse(progress.isCompleted());
    }

    @Test
    void doesNotRecordMoreThanLessonDuration() {
        CourseProgress progress = new CourseProgress(
                "Linus",
                new Lesson("构造器", 30));

        progress.recordStudyMinutes(20);
        progress.recordStudyMinutes(25);

        assertEquals(30, progress.getCompletedMinutes());
        assertEquals(0, progress.remainingMinutes());
        assertEquals(1.0, progress.completionRate(), 0.0001);
        assertTrue(progress.isCompleted());
    }

    @Test
    void buildsDeterministicSummary() {
        CourseProgress progress = new CourseProgress(
                "Grace",
                new Lesson("实例方法", 40));

        progress.recordStudyMinutes(30);

        assertEquals(
                "Grace completed 30/40 min of 实例方法 (75%)",
                progress.summary());
    }

    @Test
    void rejectsInvalidProgressState() {
        Lesson lesson = new Lesson("封装", 40);

        assertThrows(IllegalArgumentException.class,
                () -> new CourseProgress(" ", lesson));
        assertThrows(IllegalArgumentException.class,
                () -> new CourseProgress("Ada", null));

        CourseProgress progress = new CourseProgress("Ada", lesson);
        assertThrows(IllegalArgumentException.class,
                () -> progress.recordStudyMinutes(0));
    }
}
