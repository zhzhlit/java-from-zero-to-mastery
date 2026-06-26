package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudyScheduleTest {

    @Test
    void schedulesLessonsWithDateTimeAndDuration() {
        CourseLesson collections = new CourseLesson("集合", 40);
        CourseLesson fileIo = new CourseLesson("文件 I/O", 45);
        StudySchedule schedule = new StudySchedule(
                LocalDate.of(2026, 6, 26),
                LocalTime.of(20, 30),
                Duration.ofMinutes(45));

        schedule.addLesson(collections);
        schedule.addLesson(fileIo);

        assertEquals(List.of(collections, fileIo), schedule.lessons());
        assertEquals(LocalDateTime.of(2026, 6, 26, 20, 30), schedule.startsAt(1));
        assertEquals(LocalDateTime.of(2026, 6, 26, 21, 15), schedule.endsAt(1));
        assertEquals(LocalDateTime.of(2026, 6, 27, 20, 30), schedule.startsAt(2));
        assertEquals(LocalDate.of(2026, 6, 27), schedule.completionDate());
        assertEquals(5, schedule.daysUntilStart(LocalDate.of(2026, 6, 21)));
    }

    @Test
    void formatsCalendarLines() {
        StudySchedule schedule = new StudySchedule(
                LocalDate.of(2026, 7, 1),
                LocalTime.of(19, 0),
                Duration.ofMinutes(50));
        schedule.addLesson(new CourseLesson("日期与时间 API", 50));

        assertEquals("2026-07-01 19:00 - 日期与时间 API (50 min)", schedule.calendarLine(1));
    }

    @Test
    void keepsReturnedLessonsReadOnly() {
        StudySchedule schedule = new StudySchedule(
                LocalDate.of(2026, 7, 1),
                LocalTime.of(19, 0),
                Duration.ofMinutes(50));
        schedule.addLesson(new CourseLesson("日期与时间 API", 50));

        List<CourseLesson> lessons = schedule.lessons();

        assertThrows(UnsupportedOperationException.class,
                () -> lessons.add(new CourseLesson("Stream", 45)));
    }

    @Test
    void rejectsInvalidInputsAndSessionNumbers() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudySchedule(null, LocalTime.NOON, Duration.ofMinutes(30)));
        assertThrows(IllegalArgumentException.class,
                () -> new StudySchedule(LocalDate.of(2026, 7, 1), null, Duration.ofMinutes(30)));
        assertThrows(IllegalArgumentException.class,
                () -> new StudySchedule(LocalDate.of(2026, 7, 1), LocalTime.NOON, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new StudySchedule(LocalDate.of(2026, 7, 1), LocalTime.NOON, Duration.ofMinutes(-1)));

        StudySchedule schedule = new StudySchedule(
                LocalDate.of(2026, 7, 1),
                LocalTime.of(19, 0),
                Duration.ofMinutes(50));

        assertThrows(IllegalArgumentException.class,
                () -> schedule.addLesson(null));
        assertThrows(IllegalArgumentException.class,
                () -> schedule.startsAt(1));
        assertThrows(IllegalStateException.class, schedule::completionDate);
        assertThrows(IllegalArgumentException.class,
                () -> schedule.daysUntilStart(null));

        schedule.addLesson(new CourseLesson("日期与时间 API", 50));
        assertThrows(IllegalArgumentException.class,
                () -> schedule.endsAt(0));
        assertThrows(IllegalArgumentException.class,
                () -> schedule.calendarLine(2));
    }
}
