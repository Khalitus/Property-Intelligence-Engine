package edu.shu.propertyintel.dsa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AVLTreeTest {
    @Test
    void balancesAllFourRotationCases() {
        int[][] cases = {
                {30, 20, 10},
                {10, 20, 30},
                {30, 10, 20},
                {10, 30, 20}
        };
        for (int[] values : cases) {
            AVLTree<Integer, String> tree = new AVLTree<>();
            for (int value : values) {
                tree.insert(value, "P" + value);
            }
            assertEquals(20, tree.rootKey());
            assertEquals(2, tree.height());
            assertEquals(List.of("P10", "P20", "P30"), tree.rangeSearch(10, 30));
        }
    }

    @Test
    void retainsMultiplePropertiesAtSamePriceAndInclusiveBounds() {
        AVLTree<Integer, String> tree = new AVLTree<>();
        tree.insert(200, "P2");
        tree.insert(100, "P1");
        tree.insert(200, "P3");
        tree.insert(300, "P4");
        assertEquals(4, tree.size());
        assertEquals(3, tree.distinctKeys());
        assertEquals(List.of("P2", "P3"), tree.find(200));
        assertEquals(List.of("P2", "P3"), tree.rangeSearch(200, 200));
        assertEquals(List.of("P1", "P2", "P3"), tree.rangeSearch(100, 200));
        assertEquals(List.of(), tree.rangeSearch(301, 999));
        assertEquals(List.of(), tree.find(999));
        assertThrows(IllegalArgumentException.class, () -> tree.rangeSearch(5, 4));
    }

    @Test
    void staysBalancedWithManyAscendingKeys() {
        AVLTree<Integer, Integer> tree = new AVLTree<>();
        for (int i = 1; i <= 1_000; i++) {
            tree.insert(i, i);
        }
        assertEquals(1_000, tree.size());
        assertTrue(tree.height() <= 11);
        assertEquals(101, tree.rangeSearch(300, 400).size());
        assertEquals(300, tree.rangeSearch(300, 400).getFirst());
        assertEquals(400, tree.rangeSearch(300, 400).getLast());
    }

    @Test
    void handlesEmptyTreeAndNullInputs() {
        AVLTree<Integer, String> tree = new AVLTree<>();
        assertEquals(0, tree.height());
        assertNull(tree.rootKey());
        assertTrue(tree.find(1).isEmpty());
        assertTrue(tree.rangeSearch(1, 5).isEmpty());
        assertThrows(NullPointerException.class, () -> tree.insert(null, "P1"));
        assertThrows(NullPointerException.class, () -> tree.insert(1, null));
    }
}
