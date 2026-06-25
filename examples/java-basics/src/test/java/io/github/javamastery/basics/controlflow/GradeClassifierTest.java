package io.github.javamastery.basics.controlflow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradeClassifierTest {

    @ParameterizedTest
    @CsvSource({"100,A", "90,A", "89,B", "75,C", "60,D", "59,F"})
    void classifiesValidScores(int score, String expected) {
        assertEquals(expected, GradeClassifier.classify(score));
    }

    @ParameterizedTest
    @CsvSource({"-1", "101"})
    void rejectsInvalidScores(int score) {
        assertThrows(IllegalArgumentException.class, () -> GradeClassifier.classify(score));
    }
}
