package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@DisplayName("SelectionSort")
class SelectionSortTest {
    private final SelectionSort sort = new SelectionSort();

    @Test
    @DisplayName("把无序数组排成升序")
    void sortsValuesAscending() {
        assertArrayEquals(new int[] { 2, 3, 5, 8 }, sort.sort(new int[] { 5, 2, 8, 3 }));
    }

    @Test
    @DisplayName("保留重复值")
    void keepsDuplicateValues() {
        assertArrayEquals(new int[] { 1, 2, 2, 4 }, sort.sort(new int[] { 2, 4, 1, 2 }));
    }

    @Test
    @DisplayName("不修改传入数组")
    void doesNotModifyInputArray() {
        int[] values = { 9, 1, 5 };

        sort.sort(values);

        assertArrayEquals(new int[] { 9, 1, 5 }, values);
    }
}
