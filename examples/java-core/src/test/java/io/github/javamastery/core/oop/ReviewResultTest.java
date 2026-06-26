package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewResultTest {

    @Test
    void quizResultImplementsReviewContract() {
        QuizResult quiz = new QuizResult("  接口测验  ", 8, 10);

        assertEquals("接口测验", quiz.title());
        assertEquals(8, quiz.getCorrectAnswers());
        assertEquals(10, quiz.getTotalQuestions());
        assertEquals(0.8, quiz.scoreRate(), 0.0001);
        assertTrue(quiz.passed());
        assertEquals("接口测验: 8/10 - PASS", quiz.reportLine());
    }

    @Test
    void codeReviewResultUsesSameInterfaceMethods() {
        CodeReviewResult review = new CodeReviewResult("代码实验", 3, 6);

        assertEquals("代码实验", review.title());
        assertEquals(3, review.getPassedChecks());
        assertEquals(6, review.getTotalChecks());
        assertEquals(0.5, review.scoreRate(), 0.0001);
        assertFalse(review.passed());
        assertEquals("代码实验: 3/6 - RETRY", review.reportLine());
    }

    @Test
    void interfaceReferencesHandleDifferentImplementations() {
        ReviewResult[] results = {
                new QuizResult("接口基础", 9, 10),
                new CodeReviewResult("实现练习", 4, 5)
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
                "接口基础: 9/10 - PASS",
                "实现练习: 4/5 - PASS"), report.toString());
    }

    @Test
    void rejectsInvalidReviewState() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuizResult("", 1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new QuizResult("测验", 4, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new CodeReviewResult("代码", -1, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new CodeReviewResult("代码", 1, 0));
    }
}
