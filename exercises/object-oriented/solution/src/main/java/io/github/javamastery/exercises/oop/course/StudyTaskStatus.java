package io.github.javamastery.exercises.oop.course;

public enum StudyTaskStatus {
    TODO("待开始"),
    IN_PROGRESS("进行中"),
    DONE("已完成"),
    BLOCKED("受阻");

    private final String displayName;

    StudyTaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean open() {
        return this != DONE;
    }
}
