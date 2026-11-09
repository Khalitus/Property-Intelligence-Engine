package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.service.PropertySearchService;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IntelligencePanelsTest {
    @Test
    void recommendationsInitiallyDisplayEligibleProperties() throws Exception {
        edt(() -> {
            RecommendationPanel panel = new RecommendationPanel(UiTestData.store(), p -> {});
            JTable table = find(panel, "recommend.results", JTable.class);
            assertEquals(2, table.getRowCount());
            assertEquals("P0001", table.getValueAt(0, 0));
        });
    }

    @Test
    void budgetRestrictsAndResetRestoresRecommendations() throws Exception {
        edt(() -> {
            RecommendationPanel panel = new RecommendationPanel(UiTestData.store(), p -> {});
            JTextField budget = find(panel, "recommend.budget", JTextField.class);
            JTable table = find(panel, "recommend.results", JTable.class);
            budget.setText("130000");
            find(panel, "recommend.apply", JButton.class).doClick();
            assertEquals(1, table.getRowCount());
            find(panel, "recommend.reset", JButton.class).doClick();
            assertEquals(2, table.getRowCount());
        });
    }

    @Test
    void impossibleRecommendationFiltersShowNoRows() throws Exception {
        edt(() -> {
            RecommendationPanel panel = new RecommendationPanel(UiTestData.store(), p -> {});
            JSpinner bedrooms = find(panel, "recommend.bedrooms", JSpinner.class);
            bedrooms.setValue(20);
            find(panel, "recommend.apply", JButton.class).doClick();
            assertEquals(0, find(panel, "recommend.results", JTable.class).getRowCount());
            assertFalse(find(panel, "recommend.history", JButton.class).isEnabled());
        });
    }

    @Test
    void invalidRecommendationBudgetPreservesResults() throws Exception {
        edt(() -> {
            RecommendationPanel panel = new RecommendationPanel(UiTestData.store(), p -> {});
            JTextField budget = find(panel, "recommend.budget", JTextField.class);
            budget.setText("0");
            find(panel, "recommend.apply", JButton.class).doClick();
            assertEquals(2, find(panel, "recommend.results", JTable.class).getRowCount());
        });
    }

    @Test
    void valuationShowsComparablesForExistingId() throws Exception {
        edt(() -> {
            ValuationPanel panel = new ValuationPanel(UiTestData.store());
            JTextField field = find(panel, "valuation.id", JTextField.class);
            field.setText("p0003");
            find(panel, "valuation.calculate", JButton.class).doClick();
            JTable table = find(panel, "valuation.results", JTable.class);
            assertEquals(1, table.getRowCount());
            assertEquals("P0001", table.getValueAt(0, 0));
        });
    }

    @Test
    void valuationRejectsUnknownPropertyWithoutResults() throws Exception {
        edt(() -> {
            ValuationPanel panel = new ValuationPanel(UiTestData.store());
            find(panel, "valuation.id", JTextField.class).setText("missing");
            find(panel, "valuation.calculate", JButton.class).doClick();
            assertEquals(0, find(panel, "valuation.results", JTable.class).getRowCount());
        });
    }

    @Test
    void valuationClearsResultsAndRestoresDefaultCount() throws Exception {
        edt(() -> {
            ValuationPanel panel = new ValuationPanel(UiTestData.store());
            find(panel, "valuation.id", JTextField.class).setText("P0001");
            find(panel, "valuation.calculate", JButton.class).doClick();
            find(panel, "valuation.clear", JButton.class).doClick();
            assertEquals("", find(panel, "valuation.id", JTextField.class).getText());
            assertEquals(3, find(panel, "valuation.count", JSpinner.class).getValue());
            assertEquals(0, find(panel, "valuation.results", JTable.class).getRowCount());
        });
    }

    @Test
    void comparisonInitiallyExplainsRunActionAndNoResults() throws Exception {
        edt(() -> {
            PropertyStore store = UiTestData.store();
            ComparisonPanel panel = new ComparisonPanel(store, new PropertySearchService(store));
            assertTrue(find(panel, "comparison.run", JButton.class).isEnabled());
            assertEquals(0, find(panel, "comparison.results", JTable.class).getRowCount());
        });
    }

    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName()) && type.isInstance(child)) return type.cast(child);
            if (child instanceof Container container) {
                try { return find(container, name, type); }
                catch (IllegalStateException ignored) { }
            }
        }
        throw new IllegalStateException("Missing component " + name);
    }

    private static void edt(Runnable action) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try { action.run(); }
            catch (Throwable error) { failure.set(error); }
        });
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
}
