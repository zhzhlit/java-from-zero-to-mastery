package io.github.javamastery.basics.methods;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTest {

    @Test
    void calculatesDiscountedTotal() {
        assertEquals(180.0, PriceCalculator.total(100.0, 2, 0.10), 0.0001);
    }

    @Test
    void rejectsInvalidDiscount() {
        assertThrows(IllegalArgumentException.class,
                () -> PriceCalculator.total(100.0, 1, 1.1));
    }
}
