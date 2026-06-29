package io.github.javamastery.exercises.datastructures;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SimpleStringIntMap")
class SimpleStringIntMapTest {
    @Test
    @Disabled("练习：移除 @Disabled，实现保存和读取 key/value")
    @DisplayName("保存键值并按键读取")
    void storesAndReadsValuesByKey() {
        SimpleStringIntMap map = new SimpleStringIntMap();

        assertTrue(map.put("array", 3).isEmpty());

        assertEquals(3, map.get("array").orElseThrow());
        assertTrue(map.containsKey("array"));
        assertEquals(1, map.size());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现重复 key 覆盖")
    @DisplayName("重复 key 会覆盖旧值并返回旧值")
    void replacesExistingValue() {
        SimpleStringIntMap map = new SimpleStringIntMap();
        map.put("stack", 1);

        assertEquals(1, map.put("stack", 2).orElseThrow());

        assertEquals(2, map.get("stack").orElseThrow());
        assertEquals(1, map.size());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现删除 key")
    @DisplayName("删除 key 后无法继续读取")
    void removesKey() {
        SimpleStringIntMap map = new SimpleStringIntMap();
        map.put("queue", 4);

        assertEquals(4, map.remove("queue").orElseThrow());

        assertFalse(map.containsKey("queue"));
        assertTrue(map.get("queue").isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现 key 集合返回")
    @DisplayName("返回当前所有 key")
    void returnsKeys() {
        SimpleStringIntMap map = new SimpleStringIntMap();
        map.put("array", 1);
        map.put("list", 2);

        assertEquals(Set.of("array", "list"), map.keys());
    }
}
