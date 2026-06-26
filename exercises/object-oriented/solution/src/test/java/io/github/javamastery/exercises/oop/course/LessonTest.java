package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LessonTest {

    @Test
    void createsLessonWithTrimmedTitle() {
        Lesson lesson = new Lesson("  类与对象  ", 50);

        assertEquals("类与对象", lesson.getTitle());
        assertEquals(50, lesson.getDurationMinutes());
    }

    @Test
    void rejectsInvalidLesson() {
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("", 50));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("类与对象", -1));
    }
}
