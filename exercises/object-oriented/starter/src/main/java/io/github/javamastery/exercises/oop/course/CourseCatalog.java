package io.github.javamastery.exercises.oop.course;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CourseCatalog {

    private final List<Lesson> lessons = new ArrayList<>();
    private final Set<String> tags = new LinkedHashSet<>();
    private final Map<String, List<Lesson>> lessonsByTag = new LinkedHashMap<>();

    public void addLesson(Lesson lesson, List<String> lessonTags) {
    }

    public List<Lesson> lessons() {
        return List.of();
    }

    public Set<String> tags() {
        return Set.of();
    }

    public List<Lesson> lessonsForTag(String tag) {
        return List.of();
    }

    public int totalDurationMinutes() {
        return 0;
    }

    public boolean containsTag(String tag) {
        return false;
    }
}
