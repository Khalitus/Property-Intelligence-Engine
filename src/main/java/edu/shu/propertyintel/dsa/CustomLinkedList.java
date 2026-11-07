package edu.shu.propertyintel.dsa;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class CustomLinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static final class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(Objects.requireNonNull(value, "value"));
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    public void insertSorted(T value, Comparator<? super T> comparator) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(comparator, "comparator");
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else if (comparator.compare(value, head.value) < 0) {
            node.next = head;
            head = node;
        } else {
            Node<T> current = head;
            while (current.next != null && comparator.compare(current.next.value, value) <= 0) {
                current = current.next;
            }
            node.next = current.next;
            current.next = node;
            if (node.next == null) {
                tail = node;
            }
        }
        size++;
    }

    public T getFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        return head.value;
    }

    public T getLast() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }
}
