package io.github.javamastery.flagship.cli;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgressFileStore {

    public void save(Path file, StudyProgress progress) throws IOException {
        Path progressFile = requireFile(file);
        StudyProgress validatedProgress = requireProgress(progress);
        createParentDirectories(progressFile);

        Properties properties = new Properties();
        properties.setProperty("courseCode", validatedProgress.courseCode());
        properties.setProperty("startedAt", validatedProgress.startedAt().toString());
        validatedProgress.lastStudiedAt()
                .ifPresent(lastStudiedAt -> properties.setProperty("lastStudiedAt", lastStudiedAt.toString()));
        properties.setProperty("completedLessonIds", String.join(",", validatedProgress.completedLessonIds()));

        try (Writer writer = Files.newBufferedWriter(progressFile, StandardCharsets.UTF_8)) {
            properties.store(writer, "course manager progress");
        }
    }

    public StudyProgress load(Path file) throws IOException {
        Path progressFile = requireFile(file);
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(progressFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        String courseCode = requireProperty(properties, "courseCode");
        LocalDateTime startedAt = LocalDateTime.parse(requireProperty(properties, "startedAt"));
        String lastStudiedAtText = properties.getProperty("lastStudiedAt");
        LocalDateTime lastStudiedAt = lastStudiedAtText == null || lastStudiedAtText.isBlank()
                ? null
                : LocalDateTime.parse(lastStudiedAtText);
        Set<String> completedLessonIds = parseLessonIds(properties.getProperty("completedLessonIds", ""));
        return new StudyProgress(courseCode, startedAt, lastStudiedAt, completedLessonIds);
    }

    public boolean exists(Path file) {
        return Files.isRegularFile(requireFile(file));
    }

    private static Set<String> parseLessonIds(String lessonIdsText) {
        if (lessonIdsText == null || lessonIdsText.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(lessonIdsText.split(","))
                .map(Lesson::normalizeId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " must not be blank");
        }
        return value;
    }

    private static void createParentDirectories(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static Path requireFile(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        return file;
    }

    private static StudyProgress requireProgress(StudyProgress progress) {
        if (progress == null) {
            throw new IllegalArgumentException("progress must not be null");
        }
        return progress;
    }
}
