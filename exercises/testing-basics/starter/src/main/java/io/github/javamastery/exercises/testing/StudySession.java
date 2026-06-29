package io.github.javamastery.exercises.testing;

public class StudySession {
    private final String topic;
    private final int plannedMinutes;

    public StudySession(String topic, int plannedMinutes) {
        this.topic = topic;
        this.plannedMinutes = plannedMinutes;
    }

    public String topic() {
        return topic;
    }

    public int plannedMinutes() {
        return plannedMinutes;
    }

    public boolean isFocusedBlock() {
        return false;
    }

    public String summary() {
        return "";
    }
}
