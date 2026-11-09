package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.service.PropertySearchService;
import org.junit.jupiter.api.Test;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class PanelInteractionTest {
    @Test
    void searchShowsAllRecordsThenAppliesAndResetsPriceRange() throws Exception {
        onEdt(() -> {
            PropertyStore store = UiTestData.store();
            SearchPanel panel = new SearchPanel(store, new PropertySearchService(store), p -> {});
            JTable results = find(panel, "search.results", JTable.class);
            JTextField minimumPrice = find(panel, "search.minimumPrice", JTextField.class);
            JButton search = find(panel, "search.apply", JButton.class);
            JButton reset = find(panel, "search.reset", JButton.class);
            assertEquals(3, results.getRowCount());
            minimumPrice.setText("200000");
            search.doClick();
            assertEquals(1, results.getRowCount());
            assertEquals("P0002", results.getValueAt(0, 0));
            reset.doClick();
            assertEquals(3, results.getRowCount());
            assertEquals("", minimumPrice.getText());
        });
    }

    @Test
    void exactIdLookupUsesStoreWithoutOtherFilters() throws Exception {
        onEdt(() -> {
            PropertyStore store = UiTestData.store();
            SearchPanel panel = new SearchPanel(store, new PropertySearchService(store), p -> {});
            find(panel, "search.minimumPrice", JTextField.class).setText("300000");
            JTextField exactId = find(panel, "search.exactId", JTextField.class);
            exactId.setText("p0002");
            find(panel, "search.findId", JButton.class).doClick();
            JTable results = find(panel, "search.results", JTable.class);
            assertEquals(1, results.getRowCount());
            assertEquals("P0002", results.getValueAt(0, 0));
        });
    }

    @Test
    void impossibleFiltersShowEmptyResultsAndNoSelection() throws Exception {
        onEdt(() -> {
            PropertyStore store = UiTestData.store();
            SearchPanel panel = new SearchPanel(store, new PropertySearchService(store), p -> {});
            JSpinner bedrooms = find(panel, "search.minimumBedrooms", JSpinner.class);
            bedrooms.setValue(20);
            find(panel, "search.apply", JButton.class).doClick();
            JTable results = find(panel, "search.results", JTable.class);
            assertEquals(0, results.getRowCount());
            assertEquals(-1, results.getSelectedRow());
        });
    }

    @Test
    void invalidPriceRangeDoesNotErasePreviousResults() throws Exception {
        onEdt(() -> {
            PropertyStore store = UiTestData.store();
            SearchPanel panel = new SearchPanel(store, new PropertySearchService(store), p -> {});
            find(panel, "search.minimumPrice", JTextField.class).setText("300000");
            find(panel, "search.maximumPrice", JTextField.class).setText("100000");
            find(panel, "search.apply", JButton.class).doClick();
            assertEquals(3, find(panel, "search.results", JTable.class).getRowCount());
        });
    }

    @Test
    void historyDisplaysSelectedSalesAndClearsInvalidId() throws Exception {
        onEdt(() -> {
            HistoryPanel panel = new HistoryPanel(UiTestData.store());
            panel.showProperty("p0001");
            JTable results = find(panel, "history.results", JTable.class);
            assertEquals(2, results.getRowCount());
            assertEquals("+20.00%", results.getValueAt(1, 3));
            panel.showProperty("notfound");
            assertEquals(0, results.getRowCount());
        });
    }

    @Test
    void historyExplainsWhenNoSalesExist() throws Exception {
        onEdt(() -> {
            HistoryPanel panel = new HistoryPanel(UiTestData.store());
            panel.showProperty("P0003");
            assertEquals(0, find(panel, "history.results", JTable.class).getRowCount());
            boolean hasExplanation = containsLabelText(panel, "no recorded sales");
            assertTrue(hasExplanation);
        });
    }

    private static boolean containsLabelText(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof JLabel label
                    && label.getText() != null && label.getText().toLowerCase().contains(text)) {
                return true;
            }
            if (component instanceof Container child && containsLabelText(child, text)) {
                return true;
            }
        }
        return false;
    }

    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container child) {
                try {
                    return find(child, name, type);
                } catch (IllegalStateException ignored) {
                    // Continue looking in other components.
                }
            }
        }
        throw new IllegalStateException("Missing UI component " + name);
    }

    private static void onEdt(Runnable task) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
            return;
        }
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        if (failure.get() != null) {
            if (failure.get() instanceof Error error) {
                throw error;
            }
            if (failure.get() instanceof RuntimeException exception) {
                throw exception;
            }
            throw new RuntimeException(failure.get());
        }
    }
}
