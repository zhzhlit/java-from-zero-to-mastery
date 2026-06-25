package io.github.javamastery.basics.variables;

public final class TemperatureConverter {

    private TemperatureConverter() {
    }

    public static double toFahrenheit(double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }
}
