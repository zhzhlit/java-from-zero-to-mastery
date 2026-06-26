package io.github.javamastery.exercises.oop.course;

public enum StudyTaskPriority {
    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高");

    private final int weight;
    private final String displayName;

    StudyTaskPriority(int weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    public int weight() {
        return weight;
    }

    public String displayName() {
        return displayName;
    }
}
