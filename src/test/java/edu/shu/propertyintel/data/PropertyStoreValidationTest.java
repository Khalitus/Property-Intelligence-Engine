package edu.shu.propertyintel.data;

import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PropertyStoreValidationTest {
    @Test
    void rejectsNonFiniteAndNonpositiveSalePrices() {
        PropertyStore store = fixture();
        assertThrows(IllegalArgumentException.class,
                () -> store.addSale(new Sale("S1", "P1", LocalDate.now(), Double.NaN)));
        assertThrows(IllegalArgumentException.class,
                () -> store.addSale(new Sale("S2", "P1", LocalDate.now(), Double.POSITIVE_INFINITY)));
        assertThrows(IllegalArgumentException.class,
                () -> store.addSale(new Sale("S3", "P1", LocalDate.now(), 0)));
        assertThrows(IllegalArgumentException.class,
                () -> store.addSale(new Sale("S4", "P1", LocalDate.now(), -100)));
        assertEquals(0, store.saleCount());
    }

    @Test
    void rejectsInvalidPropertyMeasurements() {
        PropertyStore store = fixture();
        assertThrows(IllegalArgumentException.class,
                () -> store.addProperty(new Property("P2", "N1", "House", -1, 2, 100,
                        2000, 1, 0, false, "", "")));
        assertThrows(IllegalArgumentException.class,
                () -> store.addProperty(new Property("P3", "N1", "House", 2, 2, Double.NaN,
                        2000, 1, 0, false, "", "")));
        assertThrows(IllegalArgumentException.class,
                () -> store.addProperty(new Property("P4", "N1", "House", 2, 2, 0,
                        2000, 1, 0, false, "", "")));
        assertEquals(1, store.propertyCount());
    }

    @Test
    void rejectsInvalidNeighborhoodNumbers() {
        PropertyStore store = new PropertyStore();
        assertThrows(IllegalArgumentException.class,
                () -> store.addNeighborhood(new Neighborhood("N", "Bad", Double.NaN, 5, 5, 5)));
        assertThrows(IllegalArgumentException.class,
                () -> store.addNeighborhood(new Neighborhood("N", "Bad", -2, 5, 5, 5)));
        assertEquals(0, store.neighborhoodCount());
    }

    private static PropertyStore fixture() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N1", "North", 2, 7, 7, 7));
        store.addProperty(new Property("P1", "N1", "House", 3, 2, 120,
                2010, 1, 0, false, "", ""));
        return store;
    }
}
