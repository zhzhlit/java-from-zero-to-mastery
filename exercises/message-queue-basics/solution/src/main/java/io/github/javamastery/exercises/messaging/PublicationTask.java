package io.github.javamastery.exercises.messaging;

public record PublicationTask(CoursePublishedEvent event, int attempts) {
}
