package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewResultSelectorTest {

    @Test
    void genericMethodKeepsConcreteResultType() {
        QuizResult basics = new QuizResult("泛型基础", 8, 10);
        QuizResult practice = new QuizResult("泛型练习", 10, 10);

        QuizResult best = ReviewResultSelector.bestResult(List.of(basics, practice));

        assertSame(practice, best);
    }

    @Test
    void boundedTypeParameterWorksWithDifferentImplementations() {
        CodeReviewResult naming = new CodeReviewResult("命名检查", 4, 5);
        CodeReviewResult tests = new CodeReviewResult("测试覆盖", 5, 5);

        CodeReviewResult best = ReviewResultSelector.bestResult(List.of(naming, tests));

        assertSame(tests, best);
    }

    @Test
    void rejectsInvalidResultLists() {
        assertThrows(IllegalArgumentException.class,
                () -> ReviewResultSelector.bestResult(null));
        assertThrows(IllegalArgumentException.class,
                () -> ReviewResultSelector.bestResult(List.of()));

        List<ReviewResult> results = java.util.Arrays.asList(
                new QuizResult("泛型基础", 8, 10),
                null);
        assertThrows(IllegalArgumentException.class,
                () -> ReviewResultSelector.bestResult(results));
    }
}
