package io.github.javamastery.exercises.oop.course;

import java.time.LocalDate;
import java.util.Optional;

public class StudyTask {

    private final String title;
    private final StudyTaskStatus status;
    private final StudyTaskPriority priority;
    private final LocalDate dueDate;

    public StudyTask(String title,
                     StudyTaskStatus status,
                     StudyTaskPriority priority,
                     LocalDate dueDate) {
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String title() {
        return title;
    }

    public StudyTaskStatus status() {
        return status;
    }

    public StudyTaskPriority priority() {
        return priority;
    }

    public Optional<LocalDate> dueDate() {
        return Optional.empty();
    }

    public boolean overdueOn(LocalDate currentDate) {
        return false;
    }

    public StudyTask withStatus(StudyTaskStatus newStatus) {
        return this;
    }

    public String summary() {
        return "";
    }
}
