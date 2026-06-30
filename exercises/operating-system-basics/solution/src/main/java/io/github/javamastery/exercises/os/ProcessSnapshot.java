package io.github.javamastery.exercises.os;

public class ProcessSnapshot {
    private final int pid;
    private final String name;
    private final ProcessState state;
    private final MemorySize memory;

    public ProcessSnapshot(int pid, String name, ProcessState state, MemorySize memory) {
        if (pid <= 0) {
            throw new IllegalArgumentException("pid must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (memory == null) {
            throw new IllegalArgumentException("memory must not be null");
        }
        this.pid = pid;
        this.name = name;
        this.state = state;
        this.memory = memory;
    }

    public int pid() {
        return pid;
    }

    public String name() {
        return name;
    }

    public ProcessState state() {
        return state;
    }

    public MemorySize memory() {
        return memory;
    }
}
