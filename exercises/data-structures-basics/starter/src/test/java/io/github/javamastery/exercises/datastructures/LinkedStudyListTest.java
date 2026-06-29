package io.github.javamastery.exercises.datastructures;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LinkedStudyList")
class LinkedStudyListTest {
    @Test
    @Disabled("练习：移除 @Disabled，实现头尾添加")
    @DisplayName("支持从头部和尾部添加主题")
    void addsTopicsAtBothEnds() {
        LinkedStudyList list = new LinkedStudyList();

        list.addLast("array");
        list.addLast("stack");
        list.addFirst("complexity");

        assertEquals(List.of("complexity", "array", "stack"), list.toList());
        assertEquals(3, list.size());
        assertEquals("array", list.get(1));
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现删除头节点")
    @DisplayName("删除头部元素后更新链表顺序")
    void removesFirstTopic() {
        LinkedStudyList list = new LinkedStudyList();
        list.addLast("array");
        list.addLast("queue");

        String removed = list.removeFirst();

        assertEquals("array", removed);
        assertEquals(List.of("queue"), list.toList());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现空链表和索引边界")
    @DisplayName("空链表删除和越界读取都会抛出异常")
    void rejectsEmptyRemovalAndOutOfBoundsRead() {
        LinkedStudyList list = new LinkedStudyList();

        assertThrows(IllegalStateException.class, list::removeFirst);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }
}
