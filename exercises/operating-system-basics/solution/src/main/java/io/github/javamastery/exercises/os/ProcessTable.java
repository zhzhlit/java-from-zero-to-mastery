package io.github.javamastery.exercises.os;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessTable {
    private final List<ProcessSnapshot> processes;

    public ProcessTable(List<ProcessSnapshot> processes) {
        if (processes == null) {
            throw new IllegalArgumentException("processes must not be null");
        }
        this.processes = List.copyOf(processes);
    }

    public List<String> runningProcessNames() {
        List<String> names = new ArrayList<>();
        for (ProcessSnapshot process : processes) {
            if (process.state() == ProcessState.RUNNING) {
                names.add(process.name());
            }
        }
        return names;
    }

    public long totalMemoryBytes() {
        long total = 0L;
        for (ProcessSnapshot process : processes) {
            total = Math.addExact(total, process.memory().bytes());
        }
        return total;
    }

    public long countByState(ProcessState state) {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        long count = 0L;
        for (ProcessSnapshot process : processes) {
            if (process.state() == state) {
                count++;
            }
        }
        return count;
    }

    public Optional<ProcessSnapshot> findByPid(int pid) {
        for (ProcessSnapshot process : processes) {
            if (process.pid() == pid) {
                return Optional.of(process);
            }
        }
        return Optional.empty();
    }
}
