package io.github.javamastery.exercises.basics.methods;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeFormatterTest {

    @ParameterizedTest
    @CsvSource({"0,00:00:00", "65,00:01:05", "3661,01:01:01"})
    void formatsSeconds(int totalSeconds, String expected) {
        assertEquals(expected, TimeFormatter.format(totalSeconds));
    }

    @Test
    void rejectsNegativeSeconds() {
        assertThrows(IllegalArgumentException.class, () -> TimeFormatter.format(-1));
    }
}
