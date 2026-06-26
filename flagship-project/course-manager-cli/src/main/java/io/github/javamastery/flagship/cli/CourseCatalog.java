package io.github.javamastery.flagship.cli;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CourseCatalog {

    private final Map<String, Course> coursesByCode = new LinkedHashMap<>();

    public void addCourse(Course course) {
        Course validatedCourse = Objects.requireNonNull(course, "course must not be null");
        Course previous = coursesByCode.putIfAbsent(validatedCourse.code(), validatedCourse);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate course code: " + validatedCourse.code());
        }
    }

    public List<Course> courses() {
        return coursesByCode.values().stream()
                .sorted(courseOrder())
                .toList();
    }

    public Optional<Course> findByCode(String code) {
        return Optional.ofNullable(coursesByCode.get(Course.normalizeCode(code)));
    }

    public List<Course> searchByTag(String tag) {
        String normalizedTag = Course.normalizeTag(tag);
        return coursesByCode.values().stream()
                .filter(course -> course.hasTag(normalizedTag))
                .sorted(courseOrder())
                .toList();
    }

    public Map<CourseLevel, List<Course>> coursesByLevel() {
        Map<CourseLevel, List<Course>> grouped = new EnumMap<>(CourseLevel.class);
        for (CourseLevel level : CourseLevel.values()) {
            grouped.put(level, new ArrayList<>());
        }
        for (Course course : courses()) {
            grouped.get(course.level()).add(course);
        }
        Map<CourseLevel, List<Course>> readOnlyGroups = new EnumMap<>(CourseLevel.class);
        for (Map.Entry<CourseLevel, List<Course>> entry : grouped.entrySet()) {
            readOnlyGroups.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(readOnlyGroups);
    }

    public int totalDurationMinutes() {
        int total = 0;
        for (Course course : coursesByCode.values()) {
            total += course.totalDurationMinutes();
        }
        return total;
    }

    private static Comparator<Course> courseOrder() {
        return Comparator
                .comparing(Course::level, Comparator.comparingInt(CourseLevel::order))
                .thenComparing(Course::title)
                .thenComparing(Course::code);
    }
}
