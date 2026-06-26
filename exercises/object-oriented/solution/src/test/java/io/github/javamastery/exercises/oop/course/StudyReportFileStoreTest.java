package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyReportFileStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsUtf8ReportLines() throws IOException {
        StudyReportFileStore store = new StudyReportFileStore();
        Path reportFile = tempDir.resolve("reports").resolve("weekly.txt");

        store.save(reportFile, List.of("集合: 完成", "泛型: 复习中"));

        assertTrue(store.exists(reportFile));
        assertEquals(List.of("集合: 完成", "泛型: 复习中"), store.load(reportFile));
    }

    @Test
    void appendsLineToExistingReport() throws IOException {
        StudyReportFileStore store = new StudyReportFileStore();
        Path reportFile = tempDir.resolve("weekly.txt");

        store.save(reportFile, List.of("集合: 完成"));
        store.appendLine(reportFile, "文件 I/O: 进行中");

        assertEquals(List.of("集合: 完成", "文件 I/O: 进行中"), store.load(reportFile));
    }

    @Test
    void loadReturnsReadOnlyLines() throws IOException {
        StudyReportFileStore store = new StudyReportFileStore();
        Path reportFile = tempDir.resolve("weekly.txt");
        store.save(reportFile, List.of("泛型: 完成"));

        List<String> lines = store.load(reportFile);

        assertThrows(UnsupportedOperationException.class,
                () -> lines.add("文件 I/O: 进行中"));
    }

    @Test
    void rejectsInvalidInputsBeforeWriting() {
        StudyReportFileStore store = new StudyReportFileStore();
        Path reportFile = tempDir.resolve("weekly.txt");

        assertThrows(IllegalArgumentException.class,
                () -> store.save(null, List.of("泛型: 完成")));
        assertThrows(IllegalArgumentException.class,
                () -> store.save(reportFile, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> store.save(reportFile, List.of("泛型: 完成", "")));
        assertThrows(IllegalArgumentException.class,
                () -> store.appendLine(reportFile, null));

        assertTrue(Files.notExists(reportFile));
    }

    @Test
    void propagatesIOExceptionForUnreadableFileShape() {
        StudyReportFileStore store = new StudyReportFileStore();

        assertThrows(IOException.class,
                () -> store.load(tempDir));
    }
}
