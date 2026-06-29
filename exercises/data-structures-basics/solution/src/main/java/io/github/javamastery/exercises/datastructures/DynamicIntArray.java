package io.github.javamastery.exercises.datastructures;

public class DynamicIntArray {
    private int[] values = new int[4];
    private int size;

    public void add(int value) {
        ensureCapacity(size + 1);
        values[size] = value;
        size++;
    }

    public int get(int index) {
        checkIndex(index);
        return values[index];
    }

    public int removeAt(int index) {
        checkIndex(index);
        int removed = values[index];
        for (int current = index; current < size - 1; current++) {
            values[current] = values[current + 1];
        }
        size--;
        return removed;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return values.length;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= values.length) {
            return;
        }
        int[] expanded = new int[values.length * 2];
        System.arraycopy(values, 0, expanded, 0, size);
        values = expanded;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }
}
