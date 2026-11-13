package io.github.javamastery.exercises.messaging;

public class EventPayloadConflictException extends IllegalStateException {
    public EventPayloadConflictException(Throwable cause) {
        super("同一事件 ID 不能对应不同载荷", cause);
    }
}
