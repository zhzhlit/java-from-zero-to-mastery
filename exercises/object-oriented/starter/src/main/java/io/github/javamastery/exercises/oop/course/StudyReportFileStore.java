package io.github.javamastery.exercises.oop.course;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class StudyReportFileStore {

    public void save(Path file, List<String> reportLines) throws IOException {
    }

    public void appendLine(Path file, String reportLine) throws IOException {
    }

    public List<String> load(Path file) throws IOException {
        return List.of();
    }

    public boolean exists(Path file) {
        return false;
    }
}
