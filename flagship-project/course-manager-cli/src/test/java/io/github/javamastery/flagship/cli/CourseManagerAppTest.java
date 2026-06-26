package io.github.javamastery.flagship.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CourseManagerAppTest {

    @TempDir
    Path tempDir;

    @Test
    void runsSampleWorkflowAndPersistsProgress() throws Exception {
        Path progressFile = tempDir.resolve("progress.properties");

        List<String> firstRun = CourseManagerApp.run(progressFile, LocalDateTime.of(2026, 6, 26, 10, 0));
        List<String> secondRun = CourseManagerApp.run(progressFile, LocalDateTime.of(2026, 6, 27, 10, 0));

        assertEquals("课程数量: 2", firstRun.getFirst());
        assertTrue(firstRun.stream().anyMatch(line -> line.contains("1/3")));
        assertTrue(secondRun.stream().anyMatch(line -> line.contains("2/3")));
    }
}
