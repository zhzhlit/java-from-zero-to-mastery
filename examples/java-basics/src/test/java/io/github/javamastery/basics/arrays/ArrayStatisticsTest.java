package io.github.javamastery.basics.arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayStatisticsTest {

    @Test
    void calculatesSumAverageAndMaximum() {
        int[] values = {8, 3, 10, 7};

        assertEquals(28, ArrayStatistics.sum(values));
        assertEquals(7.0, ArrayStatistics.average(values), 0.0001);
        assertEquals(10, ArrayStatistics.maximum(values));
    }

    @Test
    void definesEmptyArrayBehavior() {
        int[] values = {};

        assertEquals(0, ArrayStatistics.sum(values));
        assertThrows(IllegalArgumentException.class,
                () -> ArrayStatistics.average(values));
        assertThrows(IllegalArgumentException.class,
                () -> ArrayStatistics.maximum(values));
    }

    @Test
    void rejectsNullArrays() {
        assertThrows(IllegalArgumentException.class,
                () -> ArrayStatistics.sum(null));
    }
}
