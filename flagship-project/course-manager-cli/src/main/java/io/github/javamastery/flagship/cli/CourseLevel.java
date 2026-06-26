package io.github.javamastery.flagship.cli;

public enum CourseLevel {

    BASICS(1, "Java 基础"),
    CORE(2, "Java 核心"),
    PROJECT(3, "综合项目");

    private final int order;
    private final String displayName;

    CourseLevel(int order, String displayName) {
        this.order = order;
        this.displayName = displayName;
    }

    public int order() {
        return order;
    }

    public String displayName() {
        return displayName;
    }
}
