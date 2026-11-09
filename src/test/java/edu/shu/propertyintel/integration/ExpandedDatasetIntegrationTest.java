package edu.shu.propertyintel.integration;

import edu.shu.propertyintel.data.DatasetLoader;
import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import edu.shu.propertyintel.service.AlgorithmComparisonService;
import edu.shu.propertyintel.service.AlgorithmComparisonReport;
import edu.shu.propertyintel.service.ComparableValuationService;
import edu.shu.propertyintel.service.PropertySearchService;
import edu.shu.propertyintel.service.RecommendationCriteria;
import edu.shu.propertyintel.service.RecommendationService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpandedDatasetIntegrationTest {
    private static final Path DATA_DIRECTORY = Path.of("data");

    @Test
    void loadedDatasetHasExpectedRecordCountsAndReferences() throws IOException {
        PropertyStore store = new DatasetLoader().load(DATA_DIRECTORY);
        assertEquals(1_500, store.propertyCount());
        assertEquals(4_751, store.saleCount());
        assertEquals(12, store.neighborhoodCount());
        int histories = 0;
        int withoutSales = 0;
        for (Property property : store.allProperties()) {
            assertTrue(store.findProperty(property.id()) != null);
            assertTrue(store.findNeighborhood(property.neighborhoodId()) != null);
            if (store.salesFor(property.id()).isEmpty()) {
                withoutSales++;
            }
            for (Sale sale : store.salesFor(property.id())) {
                assertEquals(property.id(), sale.propertyId());
                assertTrue(sale.date().getYear() >= property.yearBuilt());
                histories++;
            }
        }
        assertEquals(4_751, histories);
        assertEquals(29, withoutSales);
    }

    @Test
    void allRecordedPropertiesAppearInPriceIndex() throws IOException {
        PropertyStore store = new DatasetLoader().load(DATA_DIRECTORY);
        PropertySearchService search = new PropertySearchService(store);
        assertEquals(1_471, search.indexedPropertyCount());
        assertTrue(search.priceIndexHeight() < 25);
        assertTrue(store.findProperty("P0001") != null);
        assertTrue(store.findProperty("P1500") != null);
    }

    @Test
    void recommendationsAndRecentValuationWorkOnSubmittedDataset() throws IOException {
        PropertyStore store = new DatasetLoader().load(DATA_DIRECTORY);
        ComparableValuationService valuation = new ComparableValuationService(store);
        assertEquals(LocalDate.of(2026, 10, 27), valuation.latestDatasetSaleDate().orElseThrow());
        assertEquals(LocalDate.of(2023, 10, 27), valuation.earliestComparableSaleDate().orElseThrow());
        var estimate = valuation.estimate("P0001", 3).orElseThrow();
        assertEquals(3, estimate.comparables().size());
        for (var comparable : estimate.comparables()) {
            Sale sale = store.latestSaleFor(comparable.property().id());
            assertTrue(sale.date().compareTo(LocalDate.of(2023, 10, 27)) >= 0);
        }
        var recommendations = new RecommendationService(store).recommend(
                new RecommendationCriteria(10_000_000, 0, 0, "", ""), 5);
        assertEquals(5, recommendations.size());
        for (var item : recommendations) {
            assertTrue(store.latestSaleFor(item.property().id()) != null);
        }
    }

    @Test
    void algorithmsCompareEquivalentResultsWithFullDataset() throws IOException {
        PropertyStore store = new DatasetLoader().load(DATA_DIRECTORY);
        AlgorithmComparisonReport report = new AlgorithmComparisonService(
                store, new PropertySearchService(store)).compare();
        assertEquals(1_500, report.propertyCount());
        assertEquals(1_471, report.pricedPropertyCount());
        assertEquals(3, report.results().size());
        assertEquals(1_500, report.results().get(0).resultCount());
    }
}
