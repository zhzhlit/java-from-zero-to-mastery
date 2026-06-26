package io.github.javamastery.core.oop;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class StudyStreamAnalyzer {

    private StudyStreamAnalyzer() {
    }

    public static List<String> lessonTitlesMatching(List<CourseLesson> lessons,
                                                    Predicate<CourseLesson> matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher must not be null");
        }
        return requireLessons(lessons).stream()
                .map(StudyStreamAnalyzer::requireLesson)
                .filter(matcher)
                .map(CourseLesson::getTitle)
                .toList();
    }

    public static List<String> lessonTitlesByDurationDescending(List<CourseLesson> lessons) {
        return requireLessons(lessons).stream()
                .map(StudyStreamAnalyzer::requireLesson)
                .sorted(Comparator.comparingInt(CourseLesson::getDurationMinutes)
                        .reversed()
                        .thenComparing(CourseLesson::getTitle))
                .map(CourseLesson::getTitle)
                .toList();
    }

    public static int totalDurationMinutes(List<CourseLesson> lessons) {
        return requireLessons(lessons).stream()
                .map(StudyStreamAnalyzer::requireLesson)
                .mapToInt(CourseLesson::getDurationMinutes)
                .sum();
    }

    public static List<String> passingReportLines(List<ReviewResult> results) {
        return requireResults(results).stream()
                .map(StudyStreamAnalyzer::requireResult)
                .filter(ReviewResult::passed)
                .map(ReviewResult::reportLine)
                .toList();
    }

    public static String bestReportLine(List<ReviewResult> results) {
        List<ReviewResult> safeResults = requireResults(results);
        if (safeResults.isEmpty()) {
            throw new IllegalArgumentException("results must not be empty");
        }
        return safeResults.stream()
                .map(StudyStreamAnalyzer::requireResult)
                .max(Comparator.comparingDouble(ReviewResult::scoreRate))
                .orElseThrow()
                .reportLine();
    }

    private static List<CourseLesson> requireLessons(List<CourseLesson> lessons) {
        if (lessons == null) {
            throw new IllegalArgumentException("lessons must not be null");
        }
        return lessons;
    }

    private static CourseLesson requireLesson(CourseLesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("lesson must not be null");
        }
        return lesson;
    }

    private static List<ReviewResult> requireResults(List<ReviewResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("results must not be null");
        }
        return results;
    }

    private static ReviewResult requireResult(ReviewResult result) {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        return result;
    }
}
