package io.github.javamastery.exercises.webreview;

import java.util.List;
import java.util.Optional;

public interface CourseStore {
    List<CourseSummary> publishedCourses();

    Optional<CourseSummary> findById(long id);

    CourseSummary create(String title, int lessonCount);
}
