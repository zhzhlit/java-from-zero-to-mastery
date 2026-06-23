package io.github.javamastery.examples;

public final class HelloJava {

    private HelloJava() {
    }

    public static String greeting() {
        return "Hello, Java 21!";
    }

    public static void main(String[] args) {
        System.out.println(greeting());
    }
}
