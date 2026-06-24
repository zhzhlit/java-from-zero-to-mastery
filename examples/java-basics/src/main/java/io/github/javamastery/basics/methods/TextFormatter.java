package io.github.javamastery.basics.methods;

public final class TextFormatter {

    private TextFormatter() {
    }

    public static String label(String name, String value) {
        return label(name, value, ": ");
    }

    public static String label(String name, String value, String separator) {
        return name + separator + value;
    }
}
