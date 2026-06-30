package io.github.javamastery.exercises.os;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessTable")
class ProcessTableTest {
    private final ProcessTable table = new ProcessTable(List.of(
            new ProcessSnapshot(101, "api", ProcessState.RUNNING, MemorySize.ofMiB(128)),
            new ProcessSnapshot(102, "compiler", ProcessState.WAITING, MemorySize.ofMiB(256)),
            new ProcessSnapshot(103, "worker", ProcessState.RUNNING, MemorySize.ofMiB(64)),
            new ProcessSnapshot(104, "cleanup", ProcessState.TERMINATED, MemorySize.ofMiB(16))
    ));

    @Test
    @Disabled("练习：移除 @Disabled，列出 RUNNING 进程")
    @DisplayName("列出正在运行的进程名称")
    void listsRunningProcessNames() {
        assertEquals(List.of("api", "worker"), table.runningProcessNames());
    }

    @Test
    @Disabled("练习：移除 @Disabled，统计进程内存")
    @DisplayName("统计进程内存总量")
    void sumsProcessMemory() {
        assertEquals(MemorySize.ofMiB(464).bytes(), table.totalMemoryBytes());
    }

    @Test
    @Disabled("练习：移除 @Disabled，按状态计数并按 pid 查找")
    @DisplayName("按状态计数并按 pid 查找")
    void countsByStateAndFindsByPid() {
        assertEquals(2, table.countByState(ProcessState.RUNNING));
        assertEquals(1, table.countByState(ProcessState.WAITING));
        assertTrue(table.findByPid(102).isPresent());
        assertTrue(table.findByPid(999).isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，校验进程快照")
    @DisplayName("拒绝无效的进程快照")
    void rejectsInvalidProcessSnapshot() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProcessSnapshot(0, "api", ProcessState.RUNNING, MemorySize.ofMiB(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new ProcessSnapshot(1, "", ProcessState.RUNNING, MemorySize.ofMiB(1)));
    }
}
