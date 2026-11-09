package edu.shu.propertyintel.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SortingAlgorithmsTest {
    @Test
    void sortsAscendingDescendingAndRepeatedValues() {
        Integer[] input = {9, -4, 2, 2, 8, 0, -4, 9, 3};
        Integer[] expected = input.clone();
        Arrays.sort(expected);
        Integer[] merge = input.clone();
        Integer[] quick = input.clone();
        MergeSort.sort(merge, Comparator.naturalOrder());
        QuickSort.sort(quick, Comparator.naturalOrder());
        assertArrayEquals(expected, merge);
        assertArrayEquals(expected, quick);

        MergeSort.sort(merge, Comparator.reverseOrder());
        QuickSort.sort(quick, Comparator.reverseOrder());
        Arrays.sort(expected, Comparator.reverseOrder());
        assertArrayEquals(expected, merge);
        assertArrayEquals(expected, quick);
    }

    @Test
    void handlesEmptySingleAndAlreadySortedArrays() {
        Integer[] empty = {};
        Integer[] single = {42};
        Integer[] sorted = {1, 2, 3, 4, 5, 6, 7, 8};
        MergeSort.sort(empty, Comparator.naturalOrder());
        QuickSort.sort(empty, Comparator.naturalOrder());
        MergeSort.sort(single, Comparator.naturalOrder());
        QuickSort.sort(single, Comparator.naturalOrder());
        QuickSort.sort(sorted, Comparator.naturalOrder());
        assertEquals(0, empty.length);
        assertArrayEquals(new Integer[]{42}, single);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8}, sorted);
    }

    @Test
    void agreesWithJavaReferenceSortOnLargeRandomInput() {
        Random random = new Random(42);
        Integer[] input = new Integer[5_000];
        for (int i = 0; i < input.length; i++) {
            input[i] = random.nextInt(30);
        }
        Integer[] expected = input.clone();
        Integer[] merged = input.clone();
        Integer[] quick = input.clone();
        Arrays.sort(expected);
        MergeSort.sort(merged, Comparator.naturalOrder());
        QuickSort.sort(quick, Comparator.naturalOrder());
        assertArrayEquals(expected, merged);
        assertArrayEquals(expected, quick);
    }

    @Test
    void mergeSortRetainsOrderOfItemsWithEqualKeys() {
        record Row(int group, String id) {}
        Row[] rows = {new Row(2, "A"), new Row(1, "B"), new Row(2, "C"), new Row(1, "D")};
        MergeSort.sort(rows, Comparator.comparingInt(Row::group));
        assertArrayEquals(new String[]{"B", "D", "A", "C"},
                Arrays.stream(rows).map(Row::id).toArray(String[]::new));
    }
}
