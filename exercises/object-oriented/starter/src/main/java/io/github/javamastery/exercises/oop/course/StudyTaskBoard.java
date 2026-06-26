package io.github.javamastery.exercises.oop.course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudyTaskBoard {

    private final List<StudyTask> tasks = new ArrayList<>();

    public void addTask(StudyTask task) {
    }

    public List<StudyTask> tasks() {
        return List.of();
    }

    public List<StudyTask> openTasksByPriority() {
        return List.of();
    }

    public Optional<StudyTask> findByTitle(String title) {
        return Optional.empty();
    }

    public Optional<StudyTask> firstOverdueTask(LocalDate currentDate) {
        return Optional.empty();
    }

    public String statusSummary() {
        return "";
    }
}
