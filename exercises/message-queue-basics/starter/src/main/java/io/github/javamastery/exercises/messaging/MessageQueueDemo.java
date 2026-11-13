package io.github.javamastery.exercises.messaging;

import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public class MessageQueueDemo {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String stream = "learning:mq:demo:" + UUID.randomUUID();
        try (JedisPooled redis = new JedisPooled(host, port)) {
            CourseEventQueue queue = new CourseEventQueue(redis, stream, "notifications");
            try {
                queue.ensureGroup();
                queue.publish(new CoursePublishedEvent(UUID.randomUUID(), 1, "Java Messaging"));
                System.out.println("New messages: " + queue.readNew("worker-1", 10).size());
                System.out.println("Pending before ack: " + queue.pendingCount());
                CourseEventQueue restarted = new CourseEventQueue(redis, stream, "notifications");
                var pending = restarted.readOwnPending("worker-1", 10);
                System.out.println("Pending replay count: " + pending.size());
                for (QueuedMessage message : pending) {
                    CoursePublishedEvent event = CoursePublishedEvent.fromFields(message.fields());
                    System.out.println("Processed course: " + event.courseId());
                    if (!restarted.acknowledge(message.id())) {
                        throw new IllegalStateException("消息未被确认");
                    }
                }
                System.out.println("Pending after ack: " + restarted.pendingCount());
            } finally {
                redis.del(stream);
            }
        }
    }
}
