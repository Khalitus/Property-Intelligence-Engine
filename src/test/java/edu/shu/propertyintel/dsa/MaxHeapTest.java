package edu.shu.propertyintel.dsa;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MaxHeapTest {
    @Test
    void extractsValuesFromHighestToLowest() {
        MaxHeap<Integer> heap = new MaxHeap<>(Comparator.naturalOrder());
        for (int n : new int[]{4, 1, 7, 7, 2, 9, 3}) {
            heap.add(n);
        }
        assertEquals(7, heap.size());
        assertEquals(9, heap.peek());
        for (int expected : new int[]{9, 7, 7, 4, 3, 2, 1}) {
            assertEquals(expected, heap.poll());
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void resizesWithoutLosingTheHeapProperty() {
        MaxHeap<Integer> heap = new MaxHeap<>(Comparator.naturalOrder());
        for (int i = 0; i < 500; i++) {
            heap.add(i);
        }
        for (int i = 499; i >= 0; i--) {
            assertEquals(i, heap.poll());
        }
    }

    @Test
    void acceptsCustomComparators() {
        MaxHeap<String> heap = new MaxHeap<>(Comparator.comparingInt(String::length));
        heap.add("a");
        heap.add("longest");
        heap.add("mid");
        assertEquals("longest", heap.poll());
        assertEquals("mid", heap.poll());
        assertEquals("a", heap.poll());
    }

    @Test
    void rejectsNullValuesAndEmptyOperations() {
        MaxHeap<Integer> heap = new MaxHeap<>(Comparator.naturalOrder());
        assertThrows(NullPointerException.class, () -> heap.add(null));
        assertThrows(NoSuchElementException.class, heap::peek);
        assertThrows(NoSuchElementException.class, heap::poll);
    }
}
