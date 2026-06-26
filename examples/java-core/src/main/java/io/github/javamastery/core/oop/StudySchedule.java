package io.github.javamastery.core.oop;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudySchedule {

    private static final DateTimeFormatter CALENDAR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LocalDate firstStudyDate;
    private final LocalTime dailyStartTime;
    private final Duration sessionDuration;
    private final List<CourseLesson> lessons = new ArrayList<>();

    public StudySchedule(LocalDate firstStudyDate, LocalTime dailyStartTime, Duration sessionDuration) {
        if (firstStudyDate == null) {
            throw new IllegalArgumentException("first study date must not be null");
        }
        if (dailyStartTime == null) {
            throw new IllegalArgumentException("daily start time must not be null");
        }
        if (sessionDuration == null || sessionDuration.isZero() || sessionDuration.isNegative()) {
            throw new IllegalArgumentException("session duration must be positive");
        }
        this.firstStudyDate = firstStudyDate;
        this.dailyStartTime = dailyStartTime;
        this.sessionDuration = sessionDuration;
    }

    public void addLesson(CourseLesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("lesson must not be null");
        }
        lessons.add(lesson);
    }

    public List<CourseLesson> lessons() {
        return Collections.unmodifiableList(new ArrayList<>(lessons));
    }

    public LocalDateTime startsAt(int sessionNumber) {
        lessonAt(sessionNumber);
        LocalDate sessionDate = firstStudyDate.plusDays(sessionNumber - 1L);
        return LocalDateTime.of(sessionDate, dailyStartTime);
    }

    public LocalDateTime endsAt(int sessionNumber) {
        return startsAt(sessionNumber).plus(sessionDuration);
    }

    public LocalDate completionDate() {
        requireLessons();
        return firstStudyDate.plusDays(lessons.size() - 1L);
    }

    public long daysUntilStart(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("current date must not be null");
        }
        return ChronoUnit.DAYS.between(currentDate, firstStudyDate);
    }

    public String calendarLine(int sessionNumber) {
        CourseLesson lesson = lessonAt(sessionNumber);
        return startsAt(sessionNumber).format(CALENDAR_FORMATTER)
                + " - " + lesson.getTitle()
                + " (" + sessionDuration.toMinutes() + " min)";
    }

    private CourseLesson lessonAt(int sessionNumber) {
        if (sessionNumber <= 0 || sessionNumber > lessons.size()) {
            throw new IllegalArgumentException("session number is out of range");
        }
        return lessons.get(sessionNumber - 1);
    }

    private void requireLessons() {
        if (lessons.isEmpty()) {
            throw new IllegalStateException("schedule has no lessons");
        }
    }
}
