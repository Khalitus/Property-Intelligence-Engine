package edu.shu.propertyintel.dsa;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** A binary max-heap backed by a dynamically resized array. */
public final class MaxHeap<T> {
    private final Comparator<? super T> comparator;
    private Object[] elements = new Object[16];
    private int size;

    public MaxHeap(Comparator<? super T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T value) {
        Objects.requireNonNull(value, "value");
        if (size == elements.length) {
            elements = Arrays.copyOf(elements, elements.length * 2);
        }
        int child = size++;
        while (child > 0) {
            int parent = (child - 1) / 2;
            if (comparator.compare(value, elementAt(parent)) <= 0) {
                break;
            }
            elements[child] = elements[parent];
            child = parent;
        }
        elements[child] = value;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return elementAt(0);
    }

    public T poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        T maximum = elementAt(0);
        T last = elementAt(--size);
        elements[size] = null;
        if (size == 0) {
            return maximum;
        }
        int parent = 0;
        while (parent * 2 + 1 < size) {
            int child = parent * 2 + 1;
            int right = child + 1;
            if (right < size && comparator.compare(elementAt(right), elementAt(child)) > 0) {
                child = right;
            }
            if (comparator.compare(last, elementAt(child)) >= 0) {
                break;
            }
            elements[parent] = elements[child];
            parent = child;
        }
        elements[parent] = last;
        return maximum;
    }

    @SuppressWarnings("unchecked")
    private T elementAt(int index) {
        return (T) elements[index];
    }
}
