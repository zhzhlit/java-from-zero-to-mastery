package io.github.javamastery.exercises.webreview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCourseStore implements CourseStore {
    private final Map<Long, CourseSummary> courses = new LinkedHashMap<>();
    private long nextId = 1;

    public InMemoryCourseStore(List<CourseSummary> initialCourses) {
        for (CourseSummary course : initialCourses == null ? List.<CourseSummary>of() : initialCourses) {
            courses.put(course.id(), course);
            nextId = Math.max(nextId, course.id() + 1);
        }
    }

    @Override
    public List<CourseSummary> publishedCourses() {
        return courses.values().stream()
                .filter(CourseSummary::published)
                .sorted(Comparator.comparingLong(CourseSummary::id))
                .toList();
    }

    @Override
    public Optional<CourseSummary> findById(long id) {
        return Optional.ofNullable(courses.get(id));
    }

    @Override
    public CourseSummary create(String title, int lessonCount) {
        CourseSummary course = new CourseSummary(nextId, title, lessonCount, false);
        courses.put(nextId, course);
        nextId++;
        return course;
    }

    public List<CourseSummary> allCourses() {
        return new ArrayList<>(courses.values());
    }
}
