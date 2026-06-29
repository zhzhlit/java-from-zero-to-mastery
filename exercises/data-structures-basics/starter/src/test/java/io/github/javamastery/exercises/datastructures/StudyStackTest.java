package io.github.javamastery.exercises.datastructures;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StudyStack")
class StudyStackTest {
    @Test
    @Disabled("练习：移除 @Disabled，实现后进先出")
    @DisplayName("后进入的主题先弹出")
    void popsLastPushedTopicFirst() {
        StudyStack stack = new StudyStack();

        stack.push("array");
        stack.push("stack");

        assertEquals("stack", stack.pop());
        assertEquals("array", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现 peek 不移除元素")
    @DisplayName("查看栈顶不会移除元素")
    void peekDoesNotRemoveTopTopic() {
        StudyStack stack = new StudyStack();
        stack.push("queue");

        assertEquals("queue", stack.peek().orElseThrow());
        assertEquals(1, stack.size());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现空栈边界")
    @DisplayName("空栈弹出时抛出异常")
    void rejectsPopFromEmptyStack() {
        StudyStack stack = new StudyStack();

        assertThrows(IllegalStateException.class, stack::pop);
    }
}
