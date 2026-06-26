package io.github.javamastery.exercises.oop.course;

import java.util.List;
import java.util.function.Predicate;

public final class StudyStreamAnalyzer {

    private StudyStreamAnalyzer() {
    }

    public static List<String> lessonTitlesMatching(List<Lesson> lessons,
                                                    Predicate<Lesson> matcher) {
        return List.of();
    }

    public static List<String> lessonTitlesByDurationDescending(List<Lesson> lessons) {
        return List.of();
    }

    public static int totalDurationMinutes(List<Lesson> lessons) {
        return 0;
    }

    public static List<String> passingReportLines(List<ReviewResult> results) {
        return List.of();
    }

    public static String bestReportLine(List<ReviewResult> results) {
        return "";
    }
}
