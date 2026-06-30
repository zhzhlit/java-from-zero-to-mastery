package io.github.javamastery.exercises.os;

public class MemorySize {
    private final long bytes;

    private MemorySize(long bytes) {
        this.bytes = bytes;
    }

    public static MemorySize ofBytes(long bytes) {
        return new MemorySize(0);
    }

    public static MemorySize ofKiB(long kibibytes) {
        return new MemorySize(0);
    }

    public static MemorySize ofMiB(long mebibytes) {
        return new MemorySize(0);
    }

    public static MemorySize parse(String text) {
        return new MemorySize(0);
    }

    public long bytes() {
        return bytes;
    }

    public long toKiB() {
        return 0;
    }

    public long toMiB() {
        return 0;
    }

    public boolean fitsWithin(MemorySize limit) {
        return false;
    }
}
