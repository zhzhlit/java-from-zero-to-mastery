package io.github.javamastery.basics.controlflow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberSummationTest {

    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "5,15", "100,5050"})
    void sumsFromOneThroughLimit(int limit, long expected) {
        assertEquals(expected, NumberSummation.sumTo(limit));
    }

    @ParameterizedTest
    @CsvSource({"-1", "-20"})
    void rejectsNegativeLimits(int limit) {
        assertThrows(IllegalArgumentException.class, () -> NumberSummation.sumTo(limit));
    }
}
