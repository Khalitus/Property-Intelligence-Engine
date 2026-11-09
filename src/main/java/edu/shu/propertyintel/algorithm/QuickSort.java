package edu.shu.propertyintel.algorithm;

import java.util.Comparator;
import java.util.Objects;

public final class QuickSort {
    private QuickSort() {
    }

    public static <T> void sort(T[] values, Comparator<? super T> comparator) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(comparator, "comparator");
        quickSort(values, 0, values.length - 1, comparator);
    }

    private static <T> void quickSort(T[] values, int low, int high, Comparator<? super T> comparator) {
        while (low < high) {
            int middle = low + (high - low) / 2;
            T pivot = median(values[low], values[middle], values[high], comparator);
            int less = low;
            int current = low;
            int greater = high;
            while (current <= greater) {
                int difference = comparator.compare(values[current], pivot);
                if (difference < 0) {
                    swap(values, less++, current++);
                } else if (difference > 0) {
                    swap(values, current, greater--);
                } else {
                    current++;
                }
            }
            if (less - low < high - greater) {
                quickSort(values, low, less - 1, comparator);
                low = greater + 1;
            } else {
                quickSort(values, greater + 1, high, comparator);
                high = less - 1;
            }
        }
    }

    private static <T> T median(T first, T second, T third, Comparator<? super T> comparator) {
        if (comparator.compare(first, second) > 0) {
            T temp = first;
            first = second;
            second = temp;
        }
        if (comparator.compare(second, third) > 0) {
            T temp = second;
            second = third;
            third = temp;
        }
        return comparator.compare(first, second) > 0 ? first : second;
    }

    private static <T> void swap(T[] values, int a, int b) {
        T temp = values[a];
        values[a] = values[b];
        values[b] = temp;
    }
}
