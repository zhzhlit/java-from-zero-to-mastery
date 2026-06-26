package io.github.javamastery.exercises.oop.course;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StudySchedule {

    private final LocalDate firstStudyDate;
    private final LocalTime dailyStartTime;
    private final Duration sessionDuration;
    private final List<Lesson> lessons = new ArrayList<>();

    public StudySchedule(LocalDate firstStudyDate, LocalTime dailyStartTime, Duration sessionDuration) {
        this.firstStudyDate = firstStudyDate;
        this.dailyStartTime = dailyStartTime;
        this.sessionDuration = sessionDuration;
    }

    public void addLesson(Lesson lesson) {
    }

    public List<Lesson> lessons() {
        return List.of();
    }

    public LocalDateTime startsAt(int sessionNumber) {
        return null;
    }

    public LocalDateTime endsAt(int sessionNumber) {
        return null;
    }

    public LocalDate completionDate() {
        return null;
    }

    public long daysUntilStart(LocalDate currentDate) {
        return 0;
    }

    public String calendarLine(int sessionNumber) {
        return "";
    }
}
