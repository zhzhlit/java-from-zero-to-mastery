package io.github.javamastery.exercises.webreview;

import java.util.List;
import java.util.Optional;

public class InMemoryCourseStore implements CourseStore {
    public InMemoryCourseStore(List<CourseSummary> initialCourses) {
    }

    @Override
    public List<CourseSummary> publishedCourses() {
        return List.of();
    }

    @Override
    public Optional<CourseSummary> findById(long id) {
        return Optional.empty();
    }

    @Override
    public CourseSummary create(String title, int lessonCount) {
        return new CourseSummary(1, title, lessonCount, false);
    }

    public List<CourseSummary> allCourses() {
        return List.of();
    }
}
