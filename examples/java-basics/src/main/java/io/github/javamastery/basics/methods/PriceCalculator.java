package io.github.javamastery.basics.methods;

public final class PriceCalculator {

    private PriceCalculator() {
    }

    public static double total(double unitPrice, int quantity, double discountRate) {
        if (unitPrice < 0 || quantity < 0) {
            throw new IllegalArgumentException("price and quantity must not be negative");
        }
        if (discountRate < 0 || discountRate > 1) {
            throw new IllegalArgumentException("discount rate must be between 0 and 1");
        }
        return unitPrice * quantity * (1 - discountRate);
    }
}
