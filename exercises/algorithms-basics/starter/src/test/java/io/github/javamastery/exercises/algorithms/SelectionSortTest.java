package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@DisplayName("SelectionSort")
class SelectionSortTest {
    private final SelectionSort sort = new SelectionSort();

    @Test
    @Disabled("练习：移除 @Disabled，实现选择排序")
    @DisplayName("把无序数组排成升序")
    void sortsValuesAscending() {
        assertArrayEquals(new int[] { 2, 3, 5, 8 }, sort.sort(new int[] { 5, 2, 8, 3 }));
    }

    @Test
    @Disabled("练习：移除 @Disabled，排序时保留重复值")
    @DisplayName("保留重复值")
    void keepsDuplicateValues() {
        assertArrayEquals(new int[] { 1, 2, 2, 4 }, sort.sort(new int[] { 2, 4, 1, 2 }));
    }

    @Test
    @Disabled("练习：移除 @Disabled，不修改传入数组")
    @DisplayName("不修改传入数组")
    void doesNotModifyInputArray() {
        int[] values = { 9, 1, 5 };

        sort.sort(values);

        assertArrayEquals(new int[] { 9, 1, 5 }, values);
    }
}
