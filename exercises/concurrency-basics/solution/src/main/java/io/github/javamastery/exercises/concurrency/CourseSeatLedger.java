package io.github.javamastery.exercises.concurrency;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class CourseSeatLedger {
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> learners = new HashSet<>();
    private int capacity;
    private int available;

    public CourseSeatLedger(int capacity) {
        requireCapacity(capacity);
        this.capacity = capacity;
        this.available = capacity;
    }

    public Reservation reserve(String learner) throws InterruptedException {
        requireLearner(learner);
        lock.lockInterruptibly();
        try {
            if (learners.contains(learner)) return Reservation.ALREADY_RESERVED;
            if (available == 0) return Reservation.FULL;
            learners.add(learner);
            available--;
            return Reservation.RESERVED;
        } finally {
            lock.unlock();
        }
    }

    public boolean cancel(String learner) throws InterruptedException {
        requireLearner(learner);
        lock.lockInterruptibly();
        try {
            if (!learners.remove(learner)) return false;
            available++;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean transfer(String from, String to) throws InterruptedException {
        requireLearner(from);
        requireLearner(to);
        if (from.equals(to)) throw new IllegalArgumentException("转让双方不能相同");
        lock.lockInterruptibly();
        try {
            if (!learners.contains(from) || learners.contains(to)) return false;
            learners.remove(from);
            learners.add(to);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void resize(int newCapacity) throws InterruptedException {
        requireCapacity(newCapacity);
        lock.lockInterruptibly();
        try {
            if (newCapacity < learners.size()) throw new IllegalStateException("容量不能小于已预订人数");
            capacity = newCapacity;
            available = newCapacity - learners.size();
        } finally {
            lock.unlock();
        }
    }

    public Snapshot snapshot() {
        lock.lock();
        try {
            return new Snapshot(capacity, available, learners);
        } finally {
            lock.unlock();
        }
    }

    private static void requireLearner(String learner) {
        if (learner == null || !learner.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("学员标识必须为 1 到 64 位字母、数字、下划线或连字符");
        }
    }

    private static void requireCapacity(int capacity) {
        if (capacity < 1 || capacity > 10000) throw new IllegalArgumentException("容量必须为 1 到 10000");
    }

    public enum Reservation { RESERVED, ALREADY_RESERVED, FULL }

    public record Snapshot(int capacity, int available, Set<String> learners) {
        public Snapshot {
            requireCapacity(capacity);
            learners = Set.copyOf(learners);
            if (available < 0 || available > capacity || learners.size() != capacity - available) {
                throw new IllegalArgumentException("名额快照不满足容量约束");
            }
        }

        public int reserved() {
            return learners.size();
        }
    }
}
