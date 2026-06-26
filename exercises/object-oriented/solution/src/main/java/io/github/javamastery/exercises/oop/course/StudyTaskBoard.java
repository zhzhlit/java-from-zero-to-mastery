package io.github.javamastery.exercises.oop.course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class StudyTaskBoard {

    private final List<StudyTask> tasks = new ArrayList<>();

    public void addTask(StudyTask task) {
        tasks.add(Objects.requireNonNull(task, "task must not be null"));
    }

    public List<StudyTask> tasks() {
        return List.copyOf(tasks);
    }

    public List<StudyTask> openTasksByPriority() {
        return tasks.stream()
                .filter(task -> task.status().open())
                .sorted(taskOrder())
                .toList();
    }

    public Optional<StudyTask> findByTitle(String title) {
        String normalizedTitle = normalizeTitle(title);
        return tasks.stream()
                .filter(task -> task.title().equalsIgnoreCase(normalizedTitle))
                .findFirst();
    }

    public Optional<StudyTask> firstOverdueTask(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "current date must not be null");
        return tasks.stream()
                .filter(task -> task.overdueOn(currentDate))
                .sorted(taskOrder())
                .findFirst();
    }

    public String statusSummary() {
        Map<StudyTaskStatus, Integer> counts = new EnumMap<>(StudyTaskStatus.class);
        for (StudyTaskStatus status : StudyTaskStatus.values()) {
            counts.put(status, 0);
        }
        for (StudyTask task : tasks) {
            counts.merge(task.status(), 1, Integer::sum);
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (StudyTaskStatus status : StudyTaskStatus.values()) {
            joiner.add(status.displayName() + "=" + counts.get(status));
        }
        return joiner.toString();
    }

    private static Comparator<StudyTask> taskOrder() {
        return Comparator
                .comparing(StudyTask::priority,
                        Comparator.comparingInt(StudyTaskPriority::weight).reversed())
                .thenComparing(task -> task.dueDate().orElse(LocalDate.MAX))
                .thenComparing(StudyTask::title);
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title.strip();
    }
}
