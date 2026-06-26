package io.github.javamastery.flagship.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CourseCatalogTest {

    @Test
    void listsCoursesByLevelAndTitle() {
        CourseCatalog catalog = SampleCourses.coreReviewCatalog();

        List<Course> courses = catalog.courses();

        assertEquals("JAVA-BASICS", courses.getFirst().code());
        assertEquals("JAVA-CORE", courses.getLast().code());
    }

    @Test
    void findsCourseByNormalizedCode() {
        CourseCatalog catalog = SampleCourses.coreReviewCatalog();

        Course course = catalog.findByCode(" java-core ")
                .orElseThrow();

        assertEquals("Java 核心综合复盘", course.title());
    }

    @Test
    void searchesByNormalizedTag() {
        CourseCatalog catalog = SampleCourses.coreReviewCatalog();

        List<Course> projectCourses = catalog.searchByTag(" PROJECT ");

        assertEquals(List.of("JAVA-CORE"), projectCourses.stream().map(Course::code).toList());
    }

    @Test
    void rejectsDuplicateCourseCode() {
        CourseCatalog catalog = SampleCourses.coreReviewCatalog();
        Course duplicate = new Course(
                "java-core",
                "重复课程",
                CourseLevel.CORE,
                List.of(new Lesson("duplicate", "重复课时", 30)),
                Set.of("core"));

        assertThrows(IllegalArgumentException.class, () -> catalog.addCourse(duplicate));
    }
}
