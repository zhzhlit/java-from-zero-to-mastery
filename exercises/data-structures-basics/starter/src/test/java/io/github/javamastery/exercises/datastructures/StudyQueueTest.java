package io.github.javamastery.exercises.datastructures;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StudyQueue")
class StudyQueueTest {
    @Test
    @Disabled("练习：移除 @Disabled，实现先进先出")
    @DisplayName("先进入的主题先出队")
    void pollsFirstOfferedTopicFirst() {
        StudyQueue queue = new StudyQueue();

        queue.offer("array");
        queue.offer("queue");

        assertEquals("array", queue.poll());
        assertEquals("queue", queue.poll());
        assertTrue(queue.isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现 peek 不移除元素")
    @DisplayName("查看队首不会移除元素")
    void peekDoesNotRemoveHeadTopic() {
        StudyQueue queue = new StudyQueue();
        queue.offer("hash map");

        assertEquals("hash map", queue.peek().orElseThrow());
        assertEquals(1, queue.size());
    }

    @Test
    @Disabled("练习：移除 @Disabled，实现空队列边界")
    @DisplayName("空队列出队时抛出异常")
    void rejectsPollFromEmptyQueue() {
        StudyQueue queue = new StudyQueue();

        assertThrows(IllegalStateException.class, queue::poll);
    }
}
