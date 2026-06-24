package io.github.javamastery.basics.variables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeConversionTest {

    @Test
    void convertsWholeLongToInt() {
        assertEquals(42, TypeConversion.toIntExact(42L));
    }

    @Test
    void rejectsOverflow() {
        assertThrows(ArithmeticException.class,
                () -> TypeConversion.toIntExact((long) Integer.MAX_VALUE + 1));
    }
}
