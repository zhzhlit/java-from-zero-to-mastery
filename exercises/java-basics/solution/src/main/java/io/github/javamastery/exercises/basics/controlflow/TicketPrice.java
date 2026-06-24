package io.github.javamastery.exercises.basics.controlflow;

public final class TicketPrice {

    private TicketPrice() {
    }

    public static int calculate(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("age must not be negative");
        }
        if (age <= 5) {
            return 0;
        }
        if (age <= 17) {
            return 50;
        }
        if (age <= 64) {
            return 100;
        }
        return 60;
    }
}
