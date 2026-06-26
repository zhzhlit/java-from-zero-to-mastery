package io.github.javamastery.core.oop;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class StudyTask {

    private final String title;
    private final StudyTaskStatus status;
    private final StudyTaskPriority priority;
    private final LocalDate dueDate;

    public StudyTask(String title,
                     StudyTaskStatus status,
                     StudyTaskPriority priority,
                     LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.title = title.strip();
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
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
        return Optional.ofNullable(dueDate);
    }

    public boolean overdueOn(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "current date must not be null");
        return dueDate()
                .map(date -> date.isBefore(currentDate) && status.open())
                .orElse(false);
    }

    public StudyTask withStatus(StudyTaskStatus newStatus) {
        return new StudyTask(title,
                Objects.requireNonNull(newStatus, "status must not be null"),
                priority,
                dueDate);
    }

    public String summary() {
        StringJoiner joiner = new StringJoiner(" | ");
        joiner.add(priority.displayName())
                .add(status.displayName())
                .add(title);
        dueDate().ifPresent(date -> joiner.add("due " + date));
        return joiner.toString();
    }
}
