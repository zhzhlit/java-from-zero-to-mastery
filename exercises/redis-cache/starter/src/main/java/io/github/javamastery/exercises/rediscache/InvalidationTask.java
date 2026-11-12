package io.github.javamastery.exercises.rediscache;

public record InvalidationTask(long id, long courseId, String cacheKey, int attempts, String errorCode) {
}
