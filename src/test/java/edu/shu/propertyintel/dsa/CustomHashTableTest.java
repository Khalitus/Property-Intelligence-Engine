package edu.shu.propertyintel.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomHashTableTest {
    private record CollisionKey(String name) {
        @Override
        public int hashCode() {
            return 1;
        }
    }

    @Test
    void resolvesCollisionsAndReplacesExistingValues() {
        CustomHashTable<CollisionKey, String> table = new CustomHashTable<>(2);
        CollisionKey first = new CollisionKey("first");
        CollisionKey second = new CollisionKey("second");
        table.put(first, "A");
        table.put(second, "B");
        table.put(first, "Updated");
        assertEquals(2, table.size());
        assertEquals("Updated", table.get(first));
        assertEquals("B", table.get(second));
        assertNull(table.get(new CollisionKey("missing")));
        assertFalse(table.containsKey(new CollisionKey("missing")));
    }

    @Test
    void keepsAllEntriesAfterRepeatedResizing() {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(1);
        for (int i = 0; i < 200; i++) {
            table.put(i, "Property-" + i);
        }
        assertEquals(200, table.size());
        for (int i = 0; i < 200; i++) {
            assertEquals("Property-" + i, table.get(i));
        }
        assertThrows(NullPointerException.class, () -> table.get(null));
        assertThrows(IllegalArgumentException.class, () -> new CustomHashTable<>(0));
    }
}
