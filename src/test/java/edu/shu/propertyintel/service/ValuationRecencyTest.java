package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ValuationRecencyTest {
    @Test
    void excludesOldSaleEvenWhenItIsTheClosestProperty() {
        PropertyStore store = basicStore();
        add(store, "SUB", "House", 3, 150, null, 0);
        add(store, "OLD", "House", 3, 150, "2020-06-01", 100_000);
        add(store, "NEW", "House", 5, 240, "2025-02-01", 500_000);
        add(store, "ANCHOR", "Apartment", 2, 80, "2026-10-27", 200_000);
        ComparableValuationService service = new ComparableValuationService(store);
        ValuationResult result = service.estimate("SUB", 3).orElseThrow();
        assertEquals("NEW", result.comparables().get(0).property().id());
        assertEquals(1, result.comparables().size());
        assertEquals(500_000, result.estimatedPrice());
        assertEquals(LocalDate.of(2023, 10, 27), service.earliestComparableSaleDate().orElseThrow());
    }

    @Test
    void cutoffDayIsIncludedButDayBeforeIsExcluded() {
        PropertyStore store = basicStore();
        add(store, "SUB", "House", 3, 150, null, 0);
        add(store, "BORDER", "House", 3, 151, "2023-10-27", 300_000);
        add(store, "BEFORE", "House", 3, 150, "2023-10-26", 100_000);
        add(store, "ANCHOR", "Apartment", 1, 80, "2026-10-27", 200_000);
        ValuationResult result = new ComparableValuationService(store).estimate("SUB", 5).orElseThrow();
        assertEquals(1, result.comparables().size());
        assertEquals("BORDER", result.comparables().get(0).property().id());
    }

    @Test
    void emptyWhenNoRecentSameTypeSaleExists() {
        PropertyStore store = basicStore();
        add(store, "SUB", "House", 3, 150, null, 0);
        add(store, "STALE", "House", 3, 150, "2018-05-01", 250_000);
        add(store, "ANCHOR", "Apartment", 2, 80, "2026-10-27", 200_000);
        assertTrue(new ComparableValuationService(store).estimate("SUB", 2).isEmpty());
    }

    @Test
    void cutoffUsesLatestSaleForEachCandidateNotAnyHistoricalSale() {
        PropertyStore store = basicStore();
        add(store, "SUB", "House", 3, 150, null, 0);
        add(store, "PEER", "House", 3, 151, "2020-01-01", 150_000);
        store.addSale(new Sale("S2", "PEER", LocalDate.of(2025, 1, 1), 350_000));
        add(store, "ANCHOR", "Apartment", 2, 80, "2026-01-01", 200_000);
        assertEquals(350_000,
                new ComparableValuationService(store).estimate("SUB", 2).orElseThrow().estimatedPrice());
    }

    @Test
    void noSalesLeavesCutoffUnavailable() {
        PropertyStore store = basicStore();
        add(store, "SUB", "House", 3, 150, null, 0);
        ComparableValuationService service = new ComparableValuationService(store);
        assertTrue(service.latestDatasetSaleDate().isEmpty());
        assertTrue(service.earliestComparableSaleDate().isEmpty());
        assertTrue(service.estimate("SUB", 3).isEmpty());
    }

    private static PropertyStore basicStore() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N", "Central", 1, 8, 8, 8));
        return store;
    }

    private static void add(PropertyStore store, String id, String type, int beds,
                            double area, String date, double price) {
        store.addProperty(new Property(id, "N", type, beds, 2, area, 2010,
                1, 0, false, "", "Fixture property"));
        if (date != null) {
            store.addSale(new Sale("S" + id, id, LocalDate.parse(date), price));
        }
    }
}
