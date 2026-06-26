package io.github.javamastery.exercises.oop.course;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class StudyReportFileStore {

    public void save(Path file, List<String> reportLines) throws IOException {
        Path reportFile = requireFile(file);
        List<String> validatedLines = requireReportLines(reportLines);
        createParentDirectories(reportFile);
        Files.write(reportFile, validatedLines, StandardCharsets.UTF_8);
    }

    public void appendLine(Path file, String reportLine) throws IOException {
        Path reportFile = requireFile(file);
        String validatedLine = requireReportLine(reportLine);
        createParentDirectories(reportFile);
        Files.write(reportFile,
                List.of(validatedLine),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    public List<String> load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(requireFile(file), StandardCharsets.UTF_8);
        return Collections.unmodifiableList(lines);
    }

    public boolean exists(Path file) {
        return Files.isRegularFile(requireFile(file));
    }

    private static void createParentDirectories(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static List<String> requireReportLines(List<String> reportLines) {
        if (reportLines == null || reportLines.isEmpty()) {
            throw new IllegalArgumentException("report lines must not be empty");
        }
        for (String reportLine : reportLines) {
            requireReportLine(reportLine);
        }
        return List.copyOf(reportLines);
    }

    private static String requireReportLine(String reportLine) {
        if (reportLine == null || reportLine.isBlank()) {
            throw new IllegalArgumentException("report line must not be blank");
        }
        return reportLine;
    }

    private static Path requireFile(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        return file;
    }
}
