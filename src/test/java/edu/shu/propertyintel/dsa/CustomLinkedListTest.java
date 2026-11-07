package edu.shu.propertyintel.dsa;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CustomLinkedListTest {
    @Test
    void appendsAndTraversesValuesInOrder() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        assertTrue(list.isEmpty());
        assertThrows(NoSuchElementException.class, list::getFirst);
        list.addLast("A");
        list.addLast("B");
        assertEquals(2, list.size());
        assertEquals("A", list.getFirst());
        assertEquals("B", list.getLast());
        Iterator<String> iterator = list.iterator();
        assertEquals("A", iterator.next());
        assertEquals("B", iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void insertsInSortedOrderIncludingHeadTailAndDuplicates() {
        CustomLinkedList<Integer> list = new CustomLinkedList<>();
        for (int value : new int[]{4, 2, 6, 1, 6, 3}) {
            list.insertSorted(value, Comparator.naturalOrder());
        }
        StringBuilder result = new StringBuilder();
        for (int value : list) {
            result.append(value).append(',');
        }
        assertEquals("1,2,3,4,6,6,", result.toString());
        assertEquals(6, list.size());
        assertEquals(6, list.getLast());
    }
}
