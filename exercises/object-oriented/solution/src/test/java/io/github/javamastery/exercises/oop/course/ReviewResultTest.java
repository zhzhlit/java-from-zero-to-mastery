package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewResultTest {

    @Test
    void quizResultUsesDefaultInterfaceBehavior() {
        QuizResult quiz = new QuizResult("  接口测验  ", 7, 10);

        assertEquals("接口测验", quiz.title());
        assertEquals(7, quiz.getCorrectAnswers());
        assertEquals(10, quiz.getTotalQuestions());
        assertEquals(0.7, quiz.scoreRate(), 0.0001);
        assertTrue(quiz.passed());
        assertEquals("接口测验: 7/10 - PASS", quiz.reportLine());
    }

    @Test
    void codeReviewResultUsesSameContract() {
        CodeReviewResult review = new CodeReviewResult("代码实验", 2, 5);

        assertEquals("代码实验", review.title());
        assertEquals(2, review.getPassedChecks());
        assertEquals(5, review.getTotalChecks());
        assertEquals(0.4, review.scoreRate(), 0.0001);
        assertFalse(review.passed());
        assertEquals("代码实验: 2/5 - RETRY", review.reportLine());
    }

    @Test
    void interfaceArrayHandlesDifferentImplementations() {
        ReviewResult[] results = {
                new QuizResult("接口基础", 6, 10),
                new CodeReviewResult("实现练习", 5, 5)
        };

        int passedCount = 0;
        StringBuilder report = new StringBuilder();
        for (ReviewResult result : results) {
            if (result.passed()) {
                passedCount++;
            }
            if (!report.isEmpty()) {
                report.append(System.lineSeparator());
            }
            report.append(result.reportLine());
        }

        assertEquals(2, passedCount);
        assertEquals(String.join(System.lineSeparator(),
                "接口基础: 6/10 - PASS",
                "实现练习: 5/5 - PASS"), report.toString());
    }

    @Test
    void rejectsInvalidReviewState() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuizResult(" ", 1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizResult("测验", -1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizResult("测验", 4, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new CodeReviewResult("代码", 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new CodeReviewResult("代码", 6, 5));
    }
}
