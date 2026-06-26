package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourseLessonTest {

    @Test
    void createsLessonWithValidatedState() {
        CourseLesson lesson = new CourseLesson("  类与对象  ", 45);

        assertEquals("类与对象", lesson.getTitle());
        assertEquals(45, lesson.getDurationMinutes());
        assertEquals("类与对象 (45 min)", lesson.toDisplayText());
    }

    @Test
    void rejectsInvalidLessonState() {
        assertThrows(IllegalArgumentException.class,
                () -> new CourseLesson(" ", 45));
        assertThrows(IllegalArgumentException.class,
                () -> new CourseLesson("类与对象", 0));
    }
}
