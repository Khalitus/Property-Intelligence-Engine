package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Property;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PropertyTableModelTest {
    @Test
    void showsPropertyValuesAndLatestSale() {
        PropertyStore store = UiTestData.store();
        PropertyTableModel model = new PropertyTableModel(store);
        model.setProperties(List.of(store.findProperty("P0001")));
        assertEquals(1, model.getRowCount());
        assertEquals("P0001", model.getValueAt(0, 0));
        assertEquals("Midtown", model.getValueAt(0, 2));
        assertEquals("120,000.00", model.getValueAt(0, 5));
    }

    @Test
    void propertiesWithNoSalesAreExplained() {
        PropertyStore store = UiTestData.store();
        PropertyTableModel model = new PropertyTableModel(store);
        model.setProperties(List.of(store.findProperty("P0003")));
        assertEquals("Not recorded", model.getValueAt(0, 5));
    }

    @Test
    void resultRowsAreCopiedAndCanBeReset() {
        PropertyStore store = UiTestData.store();
        PropertyTableModel model = new PropertyTableModel(store);
        List<Property> results = new ArrayList<>();
        results.add(store.findProperty("P0001"));
        model.setProperties(results);
        results.clear();
        assertEquals(1, model.getRowCount());
        assertEquals("P0001", model.propertyAt(0).id());
        model.setProperties(List.of());
        assertEquals(0, model.getRowCount());
    }
}
