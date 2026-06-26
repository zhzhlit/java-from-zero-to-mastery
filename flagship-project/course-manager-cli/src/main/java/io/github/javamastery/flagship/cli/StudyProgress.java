package io.github.javamastery.flagship.cli;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class StudyProgress {

    private static final DateTimeFormatter SUMMARY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String courseCode;
    private final LocalDateTime startedAt;
    private final Set<String> completedLessonIds = new LinkedHashSet<>();
    private LocalDateTime lastStudiedAt;

    public StudyProgress(String courseCode) {
        this(courseCode, LocalDateTime.now(), null, Set.of());
    }

    public StudyProgress(String courseCode,
                         LocalDateTime startedAt,
                         LocalDateTime lastStudiedAt,
                         Set<String> completedLessonIds) {
        this.courseCode = Course.normalizeCode(courseCode);
        this.startedAt = Objects.requireNonNull(startedAt, "started at must not be null");
        this.lastStudiedAt = lastStudiedAt;
        if (completedLessonIds != null) {
            for (String lessonId : completedLessonIds) {
                this.completedLessonIds.add(Lesson.normalizeId(lessonId));
            }
        }
    }

    public String courseCode() {
        return courseCode;
    }

    public LocalDateTime startedAt() {
        return startedAt;
    }

    public Optional<LocalDateTime> lastStudiedAt() {
        return Optional.ofNullable(lastStudiedAt);
    }

    public Set<String> completedLessonIds() {
        return Set.copyOf(completedLessonIds);
    }

    public void completeLesson(Course course, String lessonId, LocalDateTime studiedAt) {
        Course validatedCourse = requireSameCourse(course);
        LocalDateTime validatedStudiedAt = Objects.requireNonNull(studiedAt, "studied at must not be null");
        if (validatedStudiedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("studied at must not be before started at");
        }
        Lesson lesson = validatedCourse.lessonById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("unknown lesson id: " + lessonId));

        completedLessonIds.add(lesson.id());
        lastStudiedAt = validatedStudiedAt;
    }

    public boolean completed(String lessonId) {
        return completedLessonIds.contains(Lesson.normalizeId(lessonId));
    }

    public int completionPercent(Course course) {
        Course validatedCourse = requireSameCourse(course);
        return completedCount(validatedCourse) * 100 / validatedCourse.lessonCount();
    }

    public String summary(Course course) {
        Course validatedCourse = requireSameCourse(course);
        int completedCount = completedCount(validatedCourse);
        StringJoiner joiner = new StringJoiner(" | ");
        joiner.add(validatedCourse.title())
                .add(completedCount + "/" + validatedCourse.lessonCount())
                .add(completionPercent(validatedCourse) + "%");
        lastStudiedAt().ifPresent(time -> joiner.add("last " + SUMMARY_TIME.format(time)));
        return joiner.toString();
    }

    private int completedCount(Course course) {
        int count = 0;
        for (Lesson lesson : course.lessons()) {
            if (completedLessonIds.contains(lesson.id())) {
                count++;
            }
        }
        return count;
    }

    private Course requireSameCourse(Course course) {
        Course validatedCourse = Objects.requireNonNull(course, "course must not be null");
        if (!validatedCourse.code().equals(courseCode)) {
            throw new IllegalArgumentException("course code mismatch: " + validatedCourse.code());
        }
        return validatedCourse;
    }
}
