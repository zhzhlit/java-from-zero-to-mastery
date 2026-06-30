package io.github.javamastery.exercises.os;

import java.util.List;
import java.util.Optional;

public class ProcessTable {
    public ProcessTable(List<ProcessSnapshot> processes) {
    }

    public List<String> runningProcessNames() {
        return List.of();
    }

    public long totalMemoryBytes() {
        return 0;
    }

    public long countByState(ProcessState state) {
        return 0;
    }

    public Optional<ProcessSnapshot> findByPid(int pid) {
        return Optional.empty();
    }
}
