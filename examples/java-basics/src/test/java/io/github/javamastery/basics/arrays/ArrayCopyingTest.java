package io.github.javamastery.basics.arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayCopyingTest {

    @Test
    void returnsSortedCopyWithoutChangingSource() {
        int[] source = {4, 1, 3};

        int[] result = ArrayCopying.sortedCopy(source);

        assertArrayEquals(new int[]{1, 3, 4}, result);
        assertArrayEquals(new int[]{4, 1, 3}, source);
        assertNotSame(source, result);
    }

    @Test
    void rejectsNullSource() {
        assertThrows(IllegalArgumentException.class,
                () -> ArrayCopying.sortedCopy(null));
    }
}
