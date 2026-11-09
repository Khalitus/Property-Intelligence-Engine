package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {
    @Test
    void ranksEligiblePropertiesAndHonorsTopK() {
        PropertyStore store = exampleStore();
        List<PropertyRecommendation> results = new RecommendationService(store).recommend(
                new RecommendationCriteria(650_000, 3, 100, "House", ""), 2);
        assertEquals(2, results.size());
        assertEquals("P01", results.get(0).property().id());
        assertEquals("P02", results.get(1).property().id());
        assertTrue(results.get(0).score() > results.get(1).score());
        assertTrue(results.get(0).score() >= 0 && results.get(0).score() <= 100);
    }

    @Test
    void requiresLatestSaleAndAppliesBudgetAndNeighborhoodFilters() {
        PropertyStore store = exampleStore();
        List<PropertyRecommendation> results = new RecommendationService(store).recommend(
                new RecommendationCriteria(600_000, 3, 100, "", "Midtown"), 5);
        assertEquals(List.of("P01"), results.stream().map(x -> x.property().id()).toList());
        assertTrue(new RecommendationService(store).recommend(
                new RecommendationCriteria(100_000, 3, 0, "House", ""), 5).isEmpty());
    }

    @Test
    void equalScoresUsePropertyIdsForDeterministicRanking() {
        PropertyStore store = exampleStore();
        store.addProperty(property("P00", "N01", "House", 3, 180));
        store.addSale(new Sale("S00", "P00", LocalDate.of(2025, 1, 1), 400_000));
        List<PropertyRecommendation> results = new RecommendationService(store).recommend(
                new RecommendationCriteria(650_000, 3, 100, "House", ""), 5);
        assertEquals("P00", results.get(0).property().id());
        assertEquals("P01", results.get(1).property().id());
    }

    @Test
    void rejectsBadCriteriaAndResultLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> new RecommendationCriteria(0, 0, 0, "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new RecommendationCriteria(Double.NaN, 0, 0, "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new RecommendationCriteria(100, -1, 0, "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new RecommendationCriteria(100, 0, -1, "", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new RecommendationService(exampleStore()).recommend(
                        new RecommendationCriteria(100, 0, 0, "", ""), 0));
    }

    static PropertyStore exampleStore() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        store.addNeighborhood(new Neighborhood("N02", "Lakeside", 5, 9, 7, 9));
        store.addProperty(property("P02", "N02", "House", 4, 210));
        store.addProperty(property("P01", "N01", "House", 3, 180));
        store.addProperty(property("P03", "N01", "Apartment", 2, 100));
        store.addProperty(property("P04", "N01", "House", 3, 160));
        store.addSale(new Sale("S01", "P01", LocalDate.of(2025, 1, 1), 400_000));
        store.addSale(new Sale("S02", "P02", LocalDate.of(2025, 1, 1), 600_000));
        store.addSale(new Sale("S03", "P03", LocalDate.of(2025, 1, 1), 200_000));
        return store;
    }

    static Property property(String id, String neighborhood, String type, int bedrooms, double area) {
        return new Property(id, neighborhood, type, bedrooms, 2, area, 2010,
                1, 0, false, "parking", "Sample");
    }
}
