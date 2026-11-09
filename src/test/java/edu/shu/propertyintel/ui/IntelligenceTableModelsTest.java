package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.service.BenchmarkResult;
import edu.shu.propertyintel.service.ComparableProperty;
import edu.shu.propertyintel.service.PropertyRecommendation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntelligenceTableModelsTest {
    @Test
    void recommendationRowsShowRoundedScoreAndRecordedPrice() {
        PropertyStore store = UiTestData.store();
        Property property = store.findProperty("P0001");
        RecommendationTableModel model = new RecommendationTableModel(store);
        model.setRecommendations(List.of(new PropertyRecommendation(property, 120000, 87.37)));
        assertEquals(1, model.getRowCount());
        assertEquals("P0001", model.getValueAt(0, 0));
        assertEquals("Midtown", model.getValueAt(0, 2));
        assertEquals("120,000.00", model.getValueAt(0, 5));
        assertEquals("87.4", model.getValueAt(0, 6));
    }

    @Test
    void recommendationModelCopiesInputAndClears() {
        PropertyStore store = UiTestData.store();
        RecommendationTableModel model = new RecommendationTableModel(store);
        List<PropertyRecommendation> records = new ArrayList<>();
        records.add(new PropertyRecommendation(store.findProperty("P0002"), 220000, 70));
        model.setRecommendations(records);
        records.clear();
        assertEquals(1, model.getRowCount());
        model.setRecommendations(List.of());
        assertEquals(0, model.getRowCount());
    }

    @Test
    void comparableRowsShowRelativeIndexAndPrice() {
        PropertyStore store = UiTestData.store();
        ComparableTableModel model = new ComparableTableModel(store);
        model.setComparables(List.of(new ComparableProperty(store.findProperty("P0001"), 120000, .21349)));
        assertEquals("Midtown", model.getValueAt(0, 1));
        assertEquals("120,000.00", model.getValueAt(0, 4));
        assertEquals("0.213", model.getValueAt(0, 5));
    }

    @Test
    void comparableRowsCanClear() {
        PropertyStore store = UiTestData.store();
        ComparableTableModel model = new ComparableTableModel(store);
        model.setComparables(List.of(new ComparableProperty(store.findProperty("P0001"), 120000, 1)));
        model.setComparables(List.of());
        assertEquals(0, model.getRowCount());
    }

    @Test
    void benchmarkResultsRenderActualNumbers() {
        ComparisonTableModel model = new ComparisonTableModel();
        model.setBenchmarks(List.of(new BenchmarkResult("Lookup", "Hash table", "Linear", 3, .2134, 1.45)));
        assertEquals(1, model.getRowCount());
        assertEquals("Lookup", model.getValueAt(0, 0));
        assertEquals("0.213", model.getValueAt(0, 2));
        assertEquals("1.450", model.getValueAt(0, 4));
        assertEquals(3, model.getValueAt(0, 5));
    }

    @Test
    void benchmarkModelClearsOldResults() {
        ComparisonTableModel model = new ComparisonTableModel();
        model.setBenchmarks(List.of(new BenchmarkResult("Sort", "Merge", "Quick", 2, 1, 2)));
        model.setBenchmarks(List.of());
        assertEquals(0, model.getRowCount());
    }
}
