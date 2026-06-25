package io.github.javamastery.basics.variables;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemperatureConverterTest {

    @ParameterizedTest
    @CsvSource({"0,32", "100,212", "-40,-40"})
    void convertsCelsiusToFahrenheit(double celsius, double expected) {
        assertEquals(expected, TemperatureConverter.toFahrenheit(celsius), 0.0001);
    }
}
