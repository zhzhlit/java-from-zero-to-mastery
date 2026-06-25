package io.github.javamastery.exercises.basics.controlflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketPriceTest {

    @ParameterizedTest
    @CsvSource({"0,0", "5,0", "6,50", "17,50", "18,100", "64,100", "65,60"})
    void calculatesPriceByAge(int age, int expected) {
        assertEquals(expected, TicketPrice.calculate(age));
    }

    @Test
    void rejectsNegativeAge() {
        assertThrows(IllegalArgumentException.class, () -> TicketPrice.calculate(-1));
    }
}
