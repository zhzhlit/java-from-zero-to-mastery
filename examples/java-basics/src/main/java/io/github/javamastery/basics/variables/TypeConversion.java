package io.github.javamastery.basics.variables;

public final class TypeConversion {

    private TypeConversion() {
    }

    public static int toIntExact(long value) {
        return Math.toIntExact(value);
    }
}
