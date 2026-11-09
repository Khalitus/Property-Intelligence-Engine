package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmComparisonServiceTest {
    @Test
    void comparesAllThreeAlgorithmsUsingActualRecords() {
        PropertyStore store = fixture();
        AlgorithmComparisonReport report = new AlgorithmComparisonService(
                store, new PropertySearchService(store)).compare();

        assertEquals(5, report.propertyCount());
        assertEquals(4, report.pricedPropertyCount());
        assertEquals(3, report.results().size());
        assertEquals("Property ID lookup", report.results().get(0).operation());
        assertEquals(5, report.results().get(0).resultCount());
        assertEquals("Latest-sale price range", report.results().get(1).operation());
        assertEquals(2, report.results().get(1).resultCount());
        assertEquals("Sort by area", report.results().get(2).operation());
        assertEquals(5, report.results().get(2).resultCount());
        for (BenchmarkResult row : report.results()) {
            assertTrue(Double.isFinite(row.firstMicroseconds()));
            assertTrue(Double.isFinite(row.secondMicroseconds()));
            assertTrue(row.firstMicroseconds() >= 0);
            assertTrue(row.secondMicroseconds() >= 0);
        }
    }

    @Test
    void skipsPriceRangeForPropertiesWithoutSales() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N1", "North", 2, 7, 7, 7));
        store.addProperty(property("P1", 100));
        AlgorithmComparisonReport report = new AlgorithmComparisonService(
                store, new PropertySearchService(store)).compare();
        assertEquals(0, report.pricedPropertyCount());
        assertNull(report.minimumRangePrice());
        assertNull(report.maximumRangePrice());
        assertEquals(2, report.results().size());
    }

    @Test
    void handlesEmptyStoreWithoutDivisionByZero() {
        PropertyStore store = new PropertyStore();
        AlgorithmComparisonReport report = new AlgorithmComparisonService(
                store, new PropertySearchService(store)).compare();
        assertEquals(0, report.propertyCount());
        assertTrue(report.results().isEmpty());
    }

    @Test
    void handlesIdenticalPricesAndDuplicateAvlKeys() {
        PropertyStore store = fixture();
        store.addSale(new Sale("S5", "P04", LocalDate.of(2026, 2, 1), 400_000));
        store.addSale(new Sale("S6", "P01", LocalDate.of(2026, 2, 1), 400_000));
        AlgorithmComparisonReport report = new AlgorithmComparisonService(
                store, new PropertySearchService(store)).compare();
        assertEquals(3, report.results().size());
        assertEquals(4, report.results().get(1).resultCount());
    }

    @Test
    void reportResultsAreImmutable() {
        PropertyStore store = fixture();
        AlgorithmComparisonReport report = new AlgorithmComparisonService(
                store, new PropertySearchService(store)).compare();
        assertThrows(UnsupportedOperationException.class,
                () -> report.results().add(new BenchmarkResult("x", "a", "b", 0, 0, 0)));
    }

    @Test
    void priceIndexRejectsInvalidDirectRanges() {
        PropertyStore store = fixture();
        PropertySearchService service = new PropertySearchService(store);
        assertThrows(IllegalArgumentException.class,
                () -> service.pricedPropertiesInRange(800_000, 10));
        assertThrows(IllegalArgumentException.class,
                () -> service.pricedPropertiesInRange(Double.NaN, 900_000));
        assertThrows(IllegalArgumentException.class,
                () -> service.pricedPropertiesInRange(-1, 900_000));
        assertEquals(2, service.pricedPropertiesInRange(400_000, 400_000).size());
    }

    private static PropertyStore fixture() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N1", "North", 2, 7, 7, 7));
        for (int i = 1; i <= 5; i++) {
            store.addProperty(property("P0" + i, 90 + i * 10));
        }
        store.addSale(new Sale("S1", "P01", LocalDate.of(2025, 1, 1), 200_000));
        store.addSale(new Sale("S2", "P02", LocalDate.of(2025, 1, 1), 400_000));
        store.addSale(new Sale("S3", "P03", LocalDate.of(2025, 1, 1), 400_000));
        store.addSale(new Sale("S4", "P04", LocalDate.of(2025, 1, 1), 800_000));
        return store;
    }

    private static Property property(String id, double area) {
        return new Property(id, "N1", "House", 3, 2, area, 2010, 1, 0,
                false, "parking", "Fixture");
    }
}
