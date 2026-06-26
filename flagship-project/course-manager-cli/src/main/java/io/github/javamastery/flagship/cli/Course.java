package io.github.javamastery.flagship.cli;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Course {

    private final String code;
    private final String title;
    private final CourseLevel level;
    private final List<Lesson> lessons;
    private final Set<String> tags;

    public Course(String code, String title, CourseLevel level, List<Lesson> lessons, Set<String> tags) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (lessons == null || lessons.isEmpty()) {
            throw new IllegalArgumentException("lessons must not be empty");
        }
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags must not be empty");
        }
        this.code = normalizeCode(code);
        this.title = title.strip();
        this.level = Objects.requireNonNull(level, "level must not be null");
        this.lessons = List.copyOf(lessons);
        this.tags = normalizeTags(tags);
    }

    public String code() {
        return code;
    }

    public String title() {
        return title;
    }

    public CourseLevel level() {
        return level;
    }

    public List<Lesson> lessons() {
        return List.copyOf(lessons);
    }

    public Set<String> tags() {
        return Set.copyOf(tags);
    }

    public Optional<Lesson> lessonById(String lessonId) {
        String normalizedId = Lesson.normalizeId(lessonId);
        return lessons.stream()
                .filter(lesson -> lesson.id().equals(normalizedId))
                .findFirst();
    }

    public int totalDurationMinutes() {
        int total = 0;
        for (Lesson lesson : lessons) {
            total += lesson.durationMinutes();
        }
        return total;
    }

    public int lessonCount() {
        return lessons.size();
    }

    public boolean hasTag(String tag) {
        return tags.contains(normalizeTag(tag));
    }

    static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("course code must not be blank");
        }
        return code.strip().toUpperCase(Locale.ROOT);
    }

    static String normalizeTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag must not be blank");
        }
        return tag.strip().toLowerCase(Locale.ROOT);
    }

    private static Set<String> normalizeTags(Set<String> sourceTags) {
        Set<String> normalizedTags = new LinkedHashSet<>();
        for (String tag : sourceTags) {
            normalizedTags.add(normalizeTag(tag));
        }
        return normalizedTags;
    }
}
