package io.github.javamastery.exercises.os;

import java.util.Locale;

public class MemorySize {
    private static final long KIB = 1024L;
    private static final long MIB = KIB * 1024L;
    private static final long GIB = MIB * 1024L;

    private final long bytes;

    private MemorySize(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must not be negative");
        }
        this.bytes = bytes;
    }

    public static MemorySize ofBytes(long bytes) {
        return new MemorySize(bytes);
    }

    public static MemorySize ofKiB(long kibibytes) {
        return new MemorySize(Math.multiplyExact(kibibytes, KIB));
    }

    public static MemorySize ofMiB(long mebibytes) {
        return new MemorySize(Math.multiplyExact(mebibytes, MIB));
    }

    public static MemorySize parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("memory text must not be blank");
        }

        String normalized = text.trim().replace(" ", "").toUpperCase(Locale.ROOT);
        if (normalized.endsWith("GIB")) {
            return parseWithUnit(normalized, "GIB", GIB);
        }
        if (normalized.endsWith("MIB")) {
            return parseWithUnit(normalized, "MIB", MIB);
        }
        if (normalized.endsWith("KIB")) {
            return parseWithUnit(normalized, "KIB", KIB);
        }
        if (normalized.endsWith("B")) {
            return parseWithUnit(normalized, "B", 1L);
        }
        throw new IllegalArgumentException("unsupported memory unit: " + text);
    }

    public long bytes() {
        return bytes;
    }

    public long toKiB() {
        return bytes / KIB;
    }

    public long toMiB() {
        return bytes / MIB;
    }

    public boolean fitsWithin(MemorySize limit) {
        if (limit == null) {
            throw new IllegalArgumentException("limit must not be null");
        }
        return bytes <= limit.bytes;
    }

    private static MemorySize parseWithUnit(String normalized, String unit, long multiplier) {
        String number = normalized.substring(0, normalized.length() - unit.length());
        if (number.isBlank()) {
            throw new IllegalArgumentException("memory value must include a number");
        }
        return new MemorySize(Math.multiplyExact(Long.parseLong(number), multiplier));
    }
}
