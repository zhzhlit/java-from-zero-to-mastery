package io.github.javamastery.exercises.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ScoreGrade")
class ScoreGradeTest {
    @ParameterizedTest(name = "{0} 分 => {1}")
    @CsvSource({
            "100, EXCELLENT",
            "90, EXCELLENT",
            "89, PASS",
            "60, PASS",
            "59, RETRY",
            "0, RETRY"
    })
    @DisplayName("按分数边界返回等级")
    void classifiesScoreBoundaries(int score, ScoreGrade expected) {
        assertEquals(expected, ScoreGrade.fromScore(score));
    }

    @ParameterizedTest(name = "非法分数：{0}")
    @ValueSource(ints = {-1, 101})
    @DisplayName("拒绝 0 到 100 之外的分数")
    void rejectsScoresOutsideRange(int score) {
        assertThrows(IllegalArgumentException.class, () -> ScoreGrade.fromScore(score));
    }
}
