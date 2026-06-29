package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BinarySearch")
class BinarySearchTest {
    private final BinarySearch search = new BinarySearch();

    @Test
    @Disabled("练习：移除 @Disabled，实现有序数组的二分查找")
    @DisplayName("找到中间位置的目标值")
    void findsTargetInMiddle() {
        assertEquals(2, search.find(new int[] { 3, 5, 8, 13, 21 }, 8));
    }

    @Test
    @Disabled("练习：移除 @Disabled，覆盖左右边界")
    @DisplayName("找到左右边界的目标值")
    void findsTargetsAtBothEdges() {
        int[] sortedValues = { 2, 4, 6, 8, 10 };

        assertEquals(0, search.find(sortedValues, 2));
        assertEquals(4, search.find(sortedValues, 10));
    }

    @Test
    @Disabled("练习：移除 @Disabled，找不到时返回 -1")
    @DisplayName("找不到目标值时返回 -1")
    void returnsMinusOneWhenTargetIsMissing() {
        assertEquals(-1, search.find(new int[] { 2, 4, 6, 8 }, 5));
    }

    @Test
    @Disabled("练习：移除 @Disabled，处理空数组")
    @DisplayName("空数组返回 -1")
    void returnsMinusOneForEmptyArray() {
        assertEquals(-1, search.find(new int[] {}, 1));
    }
}
