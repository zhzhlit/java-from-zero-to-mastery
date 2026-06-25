package io.github.javamastery.exercises.basics.methods;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String format(int totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("total seconds must not be negative");
        }
        int hours = totalSeconds / 3600;
        int minutes = totalSeconds % 3600 / 60;
        int seconds = totalSeconds % 60;
        return "%02d:%02d:%02d".formatted(hours, minutes, seconds);
    }
}
