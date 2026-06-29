package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LinearSearch")
class LinearSearchTest {
    private final LinearSearch search = new LinearSearch();

    @Test
    @Disabled("练习：移除 @Disabled，实现顺序查找")
    @DisplayName("找到目标值时返回索引")
    void returnsIndexWhenTargetExists() {
        assertEquals(2, search.findFirst(new int[] { 14, 21, 34, 55 }, 34));
    }

    @Test
    @Disabled("练习：移除 @Disabled，重复值返回第一个索引")
    @DisplayName("有重复值时返回第一个索引")
    void returnsFirstIndexForDuplicateValues() {
        assertEquals(1, search.findFirst(new int[] { 8, 13, 13, 21 }, 13));
    }

    @Test
    @Disabled("练习：移除 @Disabled，找不到时返回 -1")
    @DisplayName("找不到目标值时返回 -1")
    void returnsMinusOneWhenTargetIsMissing() {
        assertEquals(-1, search.findFirst(new int[] { 3, 5, 8 }, 9));
    }

    @Test
    @Disabled("练习：移除 @Disabled，处理空数组")
    @DisplayName("空数组返回 -1")
    void returnsMinusOneForEmptyArray() {
        assertEquals(-1, search.findFirst(new int[] {}, 42));
    }
}
