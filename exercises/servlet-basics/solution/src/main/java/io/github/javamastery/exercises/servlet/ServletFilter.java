package io.github.javamastery.exercises.servlet;

@FunctionalInterface
public interface ServletFilter {
    void doFilter(WebRequest request, WebResponse response, FilterChain chain);
}
