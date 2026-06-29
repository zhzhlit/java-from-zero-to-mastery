package io.github.javamastery.exercises.datastructures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SimpleStringIntMap")
class SimpleStringIntMapTest {
    @Test
    @DisplayName("保存键值并按键读取")
    void storesAndReadsValuesByKey() {
        SimpleStringIntMap map = new SimpleStringIntMap();

        assertTrue(map.put("array", 3).isEmpty());

        assertEquals(3, map.get("array").orElseThrow());
        assertTrue(map.containsKey("array"));
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("重复 key 会覆盖旧值并返回旧值")
    void replacesExistingValue() {
        SimpleStringIntMap map = new SimpleStringIntMap();
        map.put("stack", 1);

        assertEquals(1, map.put("stack", 2).orElseThrow());

        assertEquals(2, map.get("stack").orElseThrow());
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("删除 key 后无法继续读取")
    void removesKey() {
        SimpleStringIntMap map = new SimpleStringIntMap();
        map.put("queue", 4);

        assertEquals(4, map.remove("queue").orElseThrow());

        assertFalse(map.containsKey("queue"));
        assertTrue(map.get("queue").isEmpty());
    }

    @Test
    @DisplayName("返回当前所有 key")
    void returnsKeys() {
        SimpleStringIntMap map = new SimpleStringIntMap();
        map.put("array", 1);
        map.put("list", 2);

        assertEquals(Set.of("array", "list"), map.keys());
    }
}
