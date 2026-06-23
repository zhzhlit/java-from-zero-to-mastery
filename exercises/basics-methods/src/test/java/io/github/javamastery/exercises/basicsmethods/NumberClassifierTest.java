package io.github.javamastery.exercises.basicsmethods;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NumberClassifierTest {

    @ParameterizedTest
    @CsvSource({"-3,negative", "0,zero", "8,positive"})
    void classifiesNumberBySign(int input, String expected) {
        assertEquals(expected, NumberClassifier.classify(input));
    }
}
