package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ComparableValuationServiceTest {
    @Test
    void choosesNearestSameTypeSalesAndAveragesTheirPrices() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        store.addNeighborhood(new Neighborhood("N02", "Lakeside", 4, 8, 8, 8));
        add(store, "P01", "N01", "House", 3, 180, 0); // subject has no sale
        add(store, "P02", "N01", "House", 3, 181, 400_000);
        add(store, "P03", "N01", "House", 3, 190, 500_000);
        add(store, "P04", "N02", "House", 5, 280, 800_000);
        add(store, "P05", "N01", "Apartment", 3, 180, 100_000);
        ValuationResult result = new ComparableValuationService(store).estimate("p01", 2).orElseThrow();
        assertEquals("P01", result.property().id());
        assertEquals(450_000, result.estimatedPrice());
        assertEquals(2, result.comparables().size());
        assertEquals("P02", result.comparables().get(0).property().id());
        assertEquals("P03", result.comparables().get(1).property().id());
        assertTrue(result.comparables().get(0).similarity() > result.comparables().get(1).similarity());
    }

    @Test
    void handlesFewerComparablesThanRequested() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        add(store, "P01", "N01", "House", 3, 180, 0);
        add(store, "P02", "N01", "House", 4, 180, 520_000);
        ValuationResult result = new ComparableValuationService(store).estimate("P01", 3).orElseThrow();
        assertEquals(1, result.comparables().size());
        assertEquals(520_000, result.estimatedPrice());
    }

    @Test
    void returnsEmptyWhenNoComparableSalesExist() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        add(store, "P01", "N01", "House", 3, 180, 0);
        add(store, "P02", "N01", "Apartment", 3, 180, 450_000);
        assertTrue(new ComparableValuationService(store).estimate("P01", 3).isEmpty());
    }

    @Test
    void validatesPropertyIdAndComparableCount() {
        PropertyStore store = new PropertyStore();
        ComparableValuationService service = new ComparableValuationService(store);
        assertThrows(IllegalArgumentException.class, () -> service.estimate("", 3));
        assertThrows(IllegalArgumentException.class, () -> service.estimate("P999", 3));
        assertThrows(IllegalArgumentException.class, () -> service.estimate("P999", 0));
    }

    private static void add(PropertyStore store, String id, String neighborhood,
                            String type, int bedrooms, double area, double price) {
        Property property = RecommendationServiceTest.property(id, neighborhood, type, bedrooms, area);
        store.addProperty(property);
        if (price > 0) {
            store.addSale(new Sale("S" + id, id, LocalDate.of(2025, 1, 1), price));
        }
    }
}
