package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseCatalogTest {

    @Test
    void storesLessonsAndIndexesThemByTag() {
        CourseLesson classes = new CourseLesson("类与对象", 45);
        CourseLesson exceptions = new CourseLesson("异常处理", 50);
        CourseCatalog catalog = new CourseCatalog();

        catalog.addLesson(classes, List.of("core", "oop"));
        catalog.addLesson(exceptions, List.of("core", "exceptions", "oop"));

        assertEquals(List.of(classes, exceptions), catalog.lessons());
        assertEquals(Set.of("core", "oop", "exceptions"), catalog.tags());
        assertEquals(List.of(classes, exceptions), catalog.lessonsForTag(" CORE "));
        assertEquals(List.of(exceptions), catalog.lessonsForTag("exceptions"));
        assertEquals(List.of(), catalog.lessonsForTag("missing"));
        assertEquals(95, catalog.totalDurationMinutes());
    }

    @Test
    void keepsReturnedCollectionsReadOnly() {
        CourseCatalog catalog = new CourseCatalog();
        catalog.addLesson(new CourseLesson("集合", 40), List.of("core", "collections"));

        List<CourseLesson> lessons = catalog.lessons();
        Set<String> tags = catalog.tags();
        List<CourseLesson> coreLessons = catalog.lessonsForTag("core");

        assertThrows(UnsupportedOperationException.class,
                () -> lessons.add(new CourseLesson("泛型", 45)));
        assertThrows(UnsupportedOperationException.class,
                () -> tags.add("generics"));
        assertThrows(UnsupportedOperationException.class,
                () -> coreLessons.clear());
    }

    @Test
    void normalizesTagsAndIgnoresDuplicateTagsForSameLesson() {
        CourseLesson lesson = new CourseLesson("集合", 40);
        CourseCatalog catalog = new CourseCatalog();

        catalog.addLesson(lesson, List.of(" Core ", "core", "COLLECTIONS"));

        assertTrue(catalog.containsTag("collections"));
        assertEquals(Set.of("core", "collections"), catalog.tags());
        assertEquals(List.of(lesson), catalog.lessonsForTag("core"));
    }

    @Test
    void rejectsInvalidInputsBeforeChangingCatalog() {
        CourseCatalog catalog = new CourseCatalog();

        assertThrows(IllegalArgumentException.class,
                () -> catalog.addLesson(null, List.of("core")));
        assertThrows(IllegalArgumentException.class,
                () -> catalog.addLesson(new CourseLesson("集合", 40), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> catalog.addLesson(new CourseLesson("集合", 40), List.of("core", " ")));
        assertThrows(IllegalArgumentException.class,
                () -> catalog.lessonsForTag(null));

        assertEquals(List.of(), catalog.lessons());
        assertEquals(Set.of(), catalog.tags());
    }
}
