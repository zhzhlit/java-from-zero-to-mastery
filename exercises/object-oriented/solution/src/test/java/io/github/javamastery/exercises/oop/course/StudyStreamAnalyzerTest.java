package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudyStreamAnalyzerTest {

    @Test
    void filtersLessonTitlesWithLambdaPredicate() {
        List<Lesson> lessons = List.of(
                new Lesson("集合", 40),
                new Lesson("文件 I/O", 45),
                new Lesson("日期与时间 API", 50));

        List<String> titles = StudyStreamAnalyzer.lessonTitlesMatching(lessons,
                lesson -> lesson.getDurationMinutes() >= 45);

        assertEquals(List.of("文件 I/O", "日期与时间 API"), titles);
        assertThrows(UnsupportedOperationException.class,
                () -> titles.add("Stream API"));
    }

    @Test
    void sortsLessonsByDurationAndTitle() {
        List<Lesson> lessons = List.of(
                new Lesson("日期与时间 API", 50),
                new Lesson("文件 I/O", 45),
                new Lesson("泛型", 45),
                new Lesson("集合", 40));

        assertEquals(List.of("日期与时间 API", "文件 I/O", "泛型", "集合"),
                StudyStreamAnalyzer.lessonTitlesByDurationDescending(lessons));
    }

    @Test
    void sumsLessonDurationsWithPrimitiveStream() {
        List<Lesson> lessons = List.of(
                new Lesson("集合", 40),
                new Lesson("文件 I/O", 45),
                new Lesson("日期与时间 API", 50));

        assertEquals(135, StudyStreamAnalyzer.totalDurationMinutes(lessons));
        assertEquals(0, StudyStreamAnalyzer.totalDurationMinutes(List.of()));
    }

    @Test
    void buildsReportLinesForPassingResults() {
        List<ReviewResult> results = List.of(
                new QuizResult("Stream 基础", 8, 10),
                new CodeReviewResult("lambda 练习", 2, 5),
                new CodeReviewResult("排序练习", 5, 5));

        assertEquals(List.of("Stream 基础: 8/10 - PASS", "排序练习: 5/5 - PASS"),
                StudyStreamAnalyzer.passingReportLines(results));
        assertEquals("排序练习: 5/5 - PASS", StudyStreamAnalyzer.bestReportLine(results));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> StudyStreamAnalyzer.lessonTitlesMatching(null, lesson -> true));
        assertThrows(IllegalArgumentException.class,
                () -> StudyStreamAnalyzer.lessonTitlesMatching(List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> StudyStreamAnalyzer.lessonTitlesByDurationDescending(listWithNullLesson()));
        assertThrows(IllegalArgumentException.class,
                () -> StudyStreamAnalyzer.passingReportLines(null));
        assertThrows(IllegalArgumentException.class,
                () -> StudyStreamAnalyzer.passingReportLines(listWithNullResult()));
        assertThrows(IllegalArgumentException.class,
                () -> StudyStreamAnalyzer.bestReportLine(List.of()));
    }

    private static List<Lesson> listWithNullLesson() {
        return java.util.Arrays.asList(new Lesson("集合", 40), null);
    }

    private static List<ReviewResult> listWithNullResult() {
        return java.util.Arrays.asList(new QuizResult("Stream 基础", 8, 10), null);
    }
}
