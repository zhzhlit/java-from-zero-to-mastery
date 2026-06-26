package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LessonProgressTest {

    @Test
    void tracksProgressForOneLessonObject() {
        CourseLesson lesson = new CourseLesson("封装", 40);
        LessonProgress progress = new LessonProgress(lesson);

        progress.recordStudyMinutes(15);

        assertSame(lesson, progress.getLesson());
        assertEquals(15, progress.getCompletedMinutes());
        assertEquals(25, progress.remainingMinutes());
        assertEquals(0.375, progress.completionRate(), 0.0001);
        assertFalse(progress.isCompleted());
    }

    @Test
    void capsCompletedMinutesAtLessonDuration() {
        LessonProgress progress = new LessonProgress(new CourseLesson("构造器", 30));

        progress.recordStudyMinutes(20);
        progress.recordStudyMinutes(20);

        assertEquals(30, progress.getCompletedMinutes());
        assertEquals(0, progress.remainingMinutes());
        assertTrue(progress.isCompleted());
    }

    @Test
    void rejectsInvalidProgressInput() {
        assertThrows(IllegalArgumentException.class,
                () -> new LessonProgress(null));

        LessonProgress progress = new LessonProgress(new CourseLesson("实例方法", 20));
        assertThrows(IllegalArgumentException.class,
                () -> progress.recordStudyMinutes(0));
    }
}
