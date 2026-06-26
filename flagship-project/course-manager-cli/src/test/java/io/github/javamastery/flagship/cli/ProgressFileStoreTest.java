package io.github.javamastery.flagship.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProgressFileStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsProgress() throws Exception {
        Course course = SampleCourses.javaCoreReviewCourse();
        StudyProgress progress = new StudyProgress(
                course.code(),
                LocalDateTime.of(2026, 6, 26, 9, 0),
                null,
                Set.of());
        progress.completeLesson(course, "core-oop", LocalDateTime.of(2026, 6, 26, 10, 30));
        Path file = tempDir.resolve("progress.properties");

        ProgressFileStore store = new ProgressFileStore();
        store.save(file, progress);
        StudyProgress loaded = store.load(file);

        assertTrue(store.exists(file));
        assertEquals("JAVA-CORE", loaded.courseCode());
        assertTrue(loaded.completed("core-oop"));
        assertEquals(LocalDateTime.of(2026, 6, 26, 10, 30), loaded.lastStudiedAt().orElseThrow());
    }
}
