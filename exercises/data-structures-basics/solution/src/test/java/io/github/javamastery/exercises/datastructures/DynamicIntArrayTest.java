package io.github.javamastery.exercises.datastructures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DynamicIntArray")
class DynamicIntArrayTest {
    @Test
    @DisplayName("追加元素并按索引读取")
    void addsAndReadsByIndex() {
        DynamicIntArray array = new DynamicIntArray();

        array.add(21);
        array.add(34);
        array.add(55);

        assertEquals(3, array.size());
        assertEquals(21, array.get(0));
        assertEquals(55, array.get(2));
    }

    @Test
    @DisplayName("超过容量时扩容且不丢数据")
    void growsWithoutLosingValues() {
        DynamicIntArray array = new DynamicIntArray();
        int initialCapacity = array.capacity();

        for (int value = 0; value <= initialCapacity; value++) {
            array.add(value * 10);
        }

        assertTrue(array.capacity() > initialCapacity);
        assertEquals(initialCapacity + 1, array.size());
        assertEquals(0, array.get(0));
        assertEquals(initialCapacity * 10, array.get(initialCapacity));
    }

    @Test
    @DisplayName("删除中间元素后左移后续元素")
    void removesAndShiftsRemainingValues() {
        DynamicIntArray array = new DynamicIntArray();
        array.add(1);
        array.add(2);
        array.add(3);

        int removed = array.removeAt(1);

        assertEquals(2, removed);
        assertEquals(2, array.size());
        assertEquals(3, array.get(1));
    }

    @Test
    @DisplayName("访问越界索引时抛出异常")
    void rejectsOutOfBoundsIndex() {
        DynamicIntArray array = new DynamicIntArray();

        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.removeAt(-1));
    }
}
