package io.github.javamastery.exercises.datastructures;

import java.util.ArrayList;
import java.util.List;

public class LinkedStudyList {
    private Node head;
    private Node tail;
    private int size;

    public void addFirst(String topic) {
        Node node = new Node(requireTopic(topic));
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head = node;
        }
        size++;
    }

    public void addLast(String topic) {
        Node node = new Node(requireTopic(topic));
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public String removeFirst() {
        if (head == null) {
            throw new IllegalStateException("list is empty");
        }
        String topic = head.topic;
        head = head.next;
        size--;
        if (head == null) {
            tail = null;
        }
        return topic;
    }

    public String get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
        Node current = head;
        for (int currentIndex = 0; currentIndex < index; currentIndex++) {
            current = current.next;
        }
        return current.topic;
    }

    public int size() {
        return size;
    }

    public List<String> toList() {
        List<String> topics = new ArrayList<>();
        Node current = head;
        while (current != null) {
            topics.add(current.topic);
            current = current.next;
        }
        return List.copyOf(topics);
    }

    private static String requireTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic must not be blank");
        }
        return topic.strip();
    }

    private static final class Node {
        private final String topic;
        private Node next;

        private Node(String topic) {
            this.topic = topic;
        }
    }
}
