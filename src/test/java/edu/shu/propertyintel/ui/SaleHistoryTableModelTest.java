package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SaleHistoryTableModelTest {
    @Test
    void formatsConsecutivePriceChanges() {
        PropertyStore store = UiTestData.store();
        SaleHistoryTableModel model = new SaleHistoryTableModel();
        model.setSales(store.salesFor("P0001"));
        assertEquals(2, model.getRowCount());
        assertEquals("2024-01-01", model.getValueAt(0, 0));
        assertEquals("—", model.getValueAt(0, 2));
        assertEquals("+20,000.00", model.getValueAt(1, 2));
        assertEquals("+20.00%", model.getValueAt(1, 3));
    }

    @Test
    void doesNotDivideByZero() {
        SaleHistoryTableModel model = new SaleHistoryTableModel();
        model.setSales(List.of(
                new Sale("S1", "P", LocalDate.of(2024, 1, 1), 0),
                new Sale("S2", "P", LocalDate.of(2025, 1, 1), 50)));
        assertEquals("—", model.getValueAt(1, 3));
    }

    @Test
    void replacingWithEmptyHistoryClearsTheTable() {
        SaleHistoryTableModel model = new SaleHistoryTableModel();
        model.setSales(UiTestData.store().salesFor("P0001"));
        model.setSales(List.of());
        assertEquals(0, model.getRowCount());
    }
}
