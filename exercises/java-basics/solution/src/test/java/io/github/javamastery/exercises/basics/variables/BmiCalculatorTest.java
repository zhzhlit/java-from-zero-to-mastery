package io.github.javamastery.exercises.basics.variables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BmiCalculatorTest {

    @Test
    void calculatesBmi() {
        assertEquals(22.8571, BmiCalculator.calculate(70, 1.75), 0.0001);
    }

    @Test
    void rejectsNonPositiveMeasurements() {
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(0, 1.75));
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(70, 0));
    }
}
