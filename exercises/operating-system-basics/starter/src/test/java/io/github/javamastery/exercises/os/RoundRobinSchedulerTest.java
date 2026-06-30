package io.github.javamastery.exercises.os;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RoundRobinScheduler")
class RoundRobinSchedulerTest {
    private final RoundRobinScheduler scheduler = new RoundRobinScheduler();

    @Test
    @Disabled("练习：移除 @Disabled，按时间片轮流安排进程")
    @DisplayName("按时间片轮流安排进程")
    void schedulesProcessesInRoundRobinOrder() {
        Map<String, Integer> work = new LinkedHashMap<>();
        work.put("api", 50);
        work.put("compiler", 100);
        work.put("logger", 20);

        List<String> order = scheduler.executionOrder(work, 40);

        assertEquals(List.of("api", "compiler", "logger", "api", "compiler", "compiler"), order);
    }

    @Test
    @Disabled("练习：移除 @Disabled，跳过无需 CPU 时间的进程")
    @DisplayName("跳过不需要 CPU 时间的进程")
    void skipsProcessesWithoutRemainingWork() {
        Map<String, Integer> work = new LinkedHashMap<>();
        work.put("idle", 0);
        work.put("worker", 30);

        assertEquals(List.of("worker"), scheduler.executionOrder(work, 40));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝无效调度输入")
    @DisplayName("拒绝无效时间片和剩余时间")
    void rejectsInvalidSchedulingInputs() {
        assertThrows(IllegalArgumentException.class, () -> scheduler.executionOrder(Map.of("api", 10), 0));
        assertThrows(IllegalArgumentException.class, () -> scheduler.executionOrder(Map.of("api", -1), 10));
    }
}
