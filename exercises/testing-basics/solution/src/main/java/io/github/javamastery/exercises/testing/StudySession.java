package io.github.javamastery.exercises.testing;

public class StudySession {
    private final String topic;
    private final int plannedMinutes;

    public StudySession(String topic, int plannedMinutes) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic must not be blank");
        }
        if (plannedMinutes <= 0) {
            throw new IllegalArgumentException("plannedMinutes must be positive");
        }
        this.topic = topic.strip();
        this.plannedMinutes = plannedMinutes;
    }

    public String topic() {
        return topic;
    }

    public int plannedMinutes() {
        return plannedMinutes;
    }

    public boolean isFocusedBlock() {
        return plannedMinutes >= 25;
    }

    public String summary() {
        return topic + " (" + plannedMinutes + " min)";
    }
}
