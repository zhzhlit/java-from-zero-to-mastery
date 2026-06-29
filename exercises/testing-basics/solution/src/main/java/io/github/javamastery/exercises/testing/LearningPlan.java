package io.github.javamastery.exercises.testing;

import java.util.List;
import java.util.Optional;

public class LearningPlan {
    public int totalMinutes(List<StudySession> sessions) {
        return sessions.stream()
                .mapToInt(StudySession::plannedMinutes)
                .sum();
    }

    public Optional<StudySession> longestSession(List<StudySession> sessions) {
        return sessions.stream()
                .max((left, right) -> Integer.compare(left.plannedMinutes(), right.plannedMinutes()));
    }

    public boolean containsTopic(List<StudySession> sessions, String keyword) {
        String normalizedKeyword = keyword.toLowerCase();
        return sessions.stream()
                .map(StudySession::topic)
                .map(String::toLowerCase)
                .anyMatch(topic -> topic.contains(normalizedKeyword));
    }
}
