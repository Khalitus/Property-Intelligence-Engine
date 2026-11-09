package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

public final class HistoryPanel extends JPanel {
    private final PropertyStore store;
    private final JTextField propertyId = new JTextField(14);
    private final SaleHistoryTableModel historyModel = new SaleHistoryTableModel();
    private final JLabel heading = UiTheme.section("No property selected");
    private final JLabel status = UiTheme.muted("Enter an existing property ID to display its sale history.");

    public HistoryPanel(PropertyStore store) {
        this.store = store;
        setLayout(new BorderLayout(0, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(28, 28, 28, 28));
        InputRules.propertyId(propertyId);
        propertyId.setName("history.propertyId");

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        JPanel introduction = new JPanel(new BorderLayout(0, 5));
        introduction.setOpaque(false);
        introduction.add(UiTheme.heading("Sale history"), BorderLayout.NORTH);
        introduction.add(UiTheme.muted(
                "Recorded transactions are shown oldest first. Changes compare consecutive sales."), BorderLayout.SOUTH);
        top.add(introduction, BorderLayout.NORTH);

        JPanel controls = UiTheme.surface();
        controls.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 12);
        JLabel idLabel = new JLabel("Property ID");
        idLabel.setLabelFor(propertyId);
        controls.add(idLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        propertyId.setPreferredSize(new Dimension(180, 28));
        controls.add(propertyId, constraints);
        JButton load = UiTheme.button("Show history", true);
        load.setName("history.load");
        load.addActionListener(event -> showProperty(propertyId.getText()));
        propertyId.addActionListener(event -> showProperty(propertyId.getText()));
        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        controls.add(load, constraints);
        constraints.gridx = 3;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        controls.add(UiTheme.muted("Example: P0001"), constraints);
        top.add(controls, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel main = UiTheme.surface();
        main.setLayout(new BorderLayout(0, 12));
        JPanel overview = new JPanel(new BorderLayout(0, 5));
        overview.setOpaque(false);
        overview.add(heading, BorderLayout.NORTH);
        overview.add(status, BorderLayout.SOUTH);
        main.add(overview, BorderLayout.NORTH);
        JTable table = new JTable(historyModel);
        table.setName("history.results");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScroll = UiTheme.tableScroll(table);
        tableScroll.setPreferredSize(new Dimension(500, 320));
        main.add(tableScroll, BorderLayout.CENTER);
        JLabel note = UiTheme.muted("Prices are recorded sale amounts in dataset units; the first sale has no previous change.");
        note.setHorizontalAlignment(SwingConstants.LEFT);
        main.add(note, BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);
    }

    public void showProperty(String rawId) {
        String id = rawId == null ? "" : rawId.strip().toUpperCase(Locale.ROOT);
        propertyId.setText(id);
        if (id.isBlank()) {
            clear("Enter a property ID before searching.", true);
            return;
        }
        Property property = store.findProperty(id);
        if (property == null) {
            clear("No property found with ID " + id + ". Check the ID and try again.", true);
            return;
        }
        Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
        heading.setText(property.id() + " · " + property.type() + " · "
                + (neighborhood == null ? "Unknown neighborhood" : neighborhood.name()));
        historyModel.setSales(store.salesFor(id));
        int count = historyModel.getRowCount();
        Sale latest = store.latestSaleFor(id);
        if (count == 0) {
            status.setText("This property has no recorded sales.");
        } else {
            status.setText(count + (count == 1 ? " recorded sale" : " recorded sales")
                    + String.format(Locale.US, " · Latest: %,.2f on %s", latest.price(), latest.date()));
        }
        status.setForeground(UiTheme.MUTED);
    }

    private void clear(String message, boolean error) {
        heading.setText("No property selected");
        historyModel.setSales(java.util.List.of());
        status.setText(message);
        status.setForeground(error ? UiTheme.ERROR : UiTheme.MUTED);
    }
}
