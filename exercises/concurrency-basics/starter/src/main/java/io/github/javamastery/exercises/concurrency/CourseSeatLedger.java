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
        throw new UnsupportedOperationException("练习：在同一可中断锁内检查重复与余量，再预订");
    }

    public boolean cancel(String learner) throws InterruptedException {
        throw new UnsupportedOperationException("练习：原子移除预订，且只释放一次名额");
    }

    public boolean transfer(String from, String to) throws InterruptedException {
        throw new UnsupportedOperationException("练习：校验转让双方后一次性替换成员，不改变名额数");
    }

    public void resize(int newCapacity) throws InterruptedException {
        throw new UnsupportedOperationException("练习：锁内校验已预订人数，再一起调整容量和余量");
    }

    public Snapshot snapshot() {
        throw new UnsupportedOperationException("练习：在同一锁内生成不可变的一致快照");
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
