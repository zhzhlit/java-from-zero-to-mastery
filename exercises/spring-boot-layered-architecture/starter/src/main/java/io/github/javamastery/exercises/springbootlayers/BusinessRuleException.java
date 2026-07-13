package io.github.javamastery.exercises.springbootlayers;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
