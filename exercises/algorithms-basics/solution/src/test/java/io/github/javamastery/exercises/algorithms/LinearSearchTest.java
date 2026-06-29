package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LinearSearch")
class LinearSearchTest {
    private final LinearSearch search = new LinearSearch();

    @Test
    @DisplayName("找到目标值时返回索引")
    void returnsIndexWhenTargetExists() {
        assertEquals(2, search.findFirst(new int[] { 14, 21, 34, 55 }, 34));
    }

    @Test
    @DisplayName("有重复值时返回第一个索引")
    void returnsFirstIndexForDuplicateValues() {
        assertEquals(1, search.findFirst(new int[] { 8, 13, 13, 21 }, 13));
    }

    @Test
    @DisplayName("找不到目标值时返回 -1")
    void returnsMinusOneWhenTargetIsMissing() {
        assertEquals(-1, search.findFirst(new int[] { 3, 5, 8 }, 9));
    }

    @Test
    @DisplayName("空数组返回 -1")
    void returnsMinusOneForEmptyArray() {
        assertEquals(-1, search.findFirst(new int[] {}, 42));
    }
}
