package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.service.ComparableProperty;
import edu.shu.propertyintel.service.PropertyRecommendation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleDateTransparencyTest {
    @Test
    void recommendationTableShowsTheLatestRecordedSaleDate() {
        PropertyStore store = UiTestData.store();
        RecommendationTableModel model = new RecommendationTableModel(store);
        model.setRecommendations(List.of(new PropertyRecommendation(store.findProperty("P0001"), 120000, 85)));
        assertEquals(8, model.getColumnCount());
        assertEquals("Sale date", model.getColumnName(7));
        assertEquals("2025-01-01", model.getValueAt(0, 7));
        assertEquals("120,000.00", model.getValueAt(0, 5));
    }

    @Test
    void valuationTableShowsTheDateForEachComparable() {
        PropertyStore store = UiTestData.store();
        ComparableTableModel model = new ComparableTableModel(store);
        model.setComparables(List.of(new ComparableProperty(store.findProperty("P0002"), 220000, .25)));
        assertEquals(7, model.getColumnCount());
        assertEquals("Sale date", model.getColumnName(6));
        assertEquals("2025-02-01", model.getValueAt(0, 6));
        assertEquals("220,000.00", model.getValueAt(0, 4));
    }
}
