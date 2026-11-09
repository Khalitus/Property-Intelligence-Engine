package edu.shu.propertyintel.algorithm;

import java.util.Comparator;
import java.util.Objects;

public final class MergeSort {
    private MergeSort() {
    }

    public static <T> void sort(T[] values, Comparator<? super T> comparator) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(comparator, "comparator");
        T[] buffer = values.clone();
        splitAndMerge(values, buffer, 0, values.length, comparator);
    }

    private static <T> void splitAndMerge(T[] values, T[] buffer, int from, int to,
                                           Comparator<? super T> comparator) {
        if (to - from <= 1) {
            return;
        }
        int middle = from + (to - from) / 2;
        splitAndMerge(values, buffer, from, middle, comparator);
        splitAndMerge(values, buffer, middle, to, comparator);

        int left = from;
        int right = middle;
        for (int i = from; i < to; i++) {
            if (left < middle && (right >= to || comparator.compare(values[left], values[right]) <= 0)) {
                buffer[i] = values[left++];
            } else {
                buffer[i] = values[right++];
            }
        }
        System.arraycopy(buffer, from, values, from, to - from);
    }
}
