package io.github.javamastery.exercises.oop.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CourseCatalog {

    private final List<Lesson> lessons = new ArrayList<>();
    private final Set<String> tags = new LinkedHashSet<>();
    private final Map<String, List<Lesson>> lessonsByTag = new LinkedHashMap<>();

    public void addLesson(Lesson lesson, List<String> lessonTags) {
        if (lesson == null) {
            throw new IllegalArgumentException("lesson must not be null");
        }
        Set<String> normalizedTags = normalizeTags(lessonTags);

        lessons.add(lesson);
        for (String tag : normalizedTags) {
            tags.add(tag);
            List<Lesson> taggedLessons = lessonsByTag.get(tag);
            if (taggedLessons == null) {
                taggedLessons = new ArrayList<>();
                lessonsByTag.put(tag, taggedLessons);
            }
            taggedLessons.add(lesson);
        }
    }

    public List<Lesson> lessons() {
        return Collections.unmodifiableList(new ArrayList<>(lessons));
    }

    public Set<String> tags() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(tags));
    }

    public List<Lesson> lessonsForTag(String tag) {
        String normalizedTag = normalizeTag(tag);
        List<Lesson> taggedLessons = lessonsByTag.get(normalizedTag);
        if (taggedLessons == null) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(taggedLessons));
    }

    public int totalDurationMinutes() {
        int total = 0;
        for (Lesson lesson : lessons) {
            total += lesson.getDurationMinutes();
        }
        return total;
    }

    public boolean containsTag(String tag) {
        return tags.contains(normalizeTag(tag));
    }

    private static Set<String> normalizeTags(List<String> lessonTags) {
        if (lessonTags == null || lessonTags.isEmpty()) {
            throw new IllegalArgumentException("lesson tags must not be empty");
        }

        Set<String> normalizedTags = new LinkedHashSet<>();
        for (String tag : lessonTags) {
            normalizedTags.add(normalizeTag(tag));
        }
        return normalizedTags;
    }

    private static String normalizeTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag must not be blank");
        }
        return tag.strip().toLowerCase(Locale.ROOT);
    }
}
