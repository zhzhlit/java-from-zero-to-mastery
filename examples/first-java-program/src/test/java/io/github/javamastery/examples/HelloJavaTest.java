package io.github.javamastery.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class HelloJavaTest {

    @Test
    void returnsJava21Greeting() {
        assertEquals("Hello, Java 21!", HelloJava.greeting());
    }
}
