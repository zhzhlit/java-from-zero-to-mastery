package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@DisplayName("InsertionSort")
class InsertionSortTest {
    private final InsertionSort sort = new InsertionSort();

    @Test
    @DisplayName("把无序数组排成升序")
    void sortsValuesAscending() {
        assertArrayEquals(new int[] { 1, 3, 4, 7 }, sort.sort(new int[] { 7, 3, 4, 1 }));
    }

    @Test
    @DisplayName("处理已经有序的小数组")
    void keepsAlreadySortedValues() {
        assertArrayEquals(new int[] { 1, 2, 3 }, sort.sort(new int[] { 1, 2, 3 }));
    }

    @Test
    @DisplayName("处理空数组和单元素数组")
    void handlesTinyArrays() {
        assertArrayEquals(new int[] {}, sort.sort(new int[] {}));
        assertArrayEquals(new int[] { 42 }, sort.sort(new int[] { 42 }));
    }
}
