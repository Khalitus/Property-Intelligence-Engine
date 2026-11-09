package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.service.ComparableValuationService;
import edu.shu.propertyintel.service.ValuationResult;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.Locale;
import java.util.Optional;

public final class ValuationPanel extends JPanel {
    private final PropertyStore store;
    private final ComparableValuationService service;
    private final ComparableTableModel model;
    private final JTextField id = new JTextField(13);
    private final JSpinner comparableCount = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
    private final JLabel estimate = UiTheme.heading("No estimate yet");
    private final JLabel resultInfo = UiTheme.muted("Enter an existing property ID to calculate an estimate.");
    private final JLabel status = UiTheme.muted("The system selects the closest same-type properties with recorded sales.");

    public ValuationPanel(PropertyStore store) {
        this.store = store;
        service = new ComparableValuationService(store);
        model = new ComparableTableModel(store);
        setLayout(new BorderLayout(0, 15));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(26, 25, 25, 25));
        InputRules.propertyId(id);
        id.setName("valuation.id");
        comparableCount.setName("valuation.count");
        comparableCount.setToolTipText("Use between 1 and 5 comparable properties (default: 3).");

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(UiTheme.heading("Comparable-property valuation"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Estimate from recent same-type sales; older transactions are excluded."), BorderLayout.SOUTH);
        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(buildForm(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel content = UiTheme.surface();
        content.setLayout(new BorderLayout(0, 15));
        JPanel estimatePanel = new JPanel(new BorderLayout(0, 7));
        estimatePanel.setOpaque(false);
        estimatePanel.add(UiTheme.section("Estimated property value"), BorderLayout.NORTH);
        estimate.setFont(estimate.getFont().deriveFont(Font.BOLD, 29f));
        estimatePanel.add(estimate, BorderLayout.CENTER);
        estimatePanel.add(resultInfo, BorderLayout.SOUTH);
        content.add(estimatePanel, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setName("valuation.results");
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().moveColumn(6, 5);
        int[] widths = {92, 124, 84, 88, 120, 105, 88};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JPanel resultsPanel = new JPanel(new BorderLayout(0, 10));
        resultsPanel.setOpaque(false);
        resultsPanel.add(UiTheme.section("Selected comparable sales"), BorderLayout.NORTH);
        JScrollPane scroll = UiTheme.tableScroll(table);
        scroll.setPreferredSize(new Dimension(560, 220));
        resultsPanel.add(scroll, BorderLayout.CENTER);
        resultsPanel.add(status, BorderLayout.SOUTH);
        content.add(resultsPanel, BorderLayout.CENTER);

        String earliest = service.earliestComparableSaleDate()
                .map(Object::toString).orElse("not available");
        String latest = service.latestDatasetSaleDate()
                .map(Object::toString).orElse("not available");
        JTextArea note = new JTextArea("Method: select recorded sales on or after " + earliest
                + " (three years before the dataset's latest sale, " + latest + "). "
                + "Exclude the subject and other property types; rank by bedroom, area, bathroom, parking and neighborhood differences; "
                + "average up to the requested number of comparables. Older sales are excluded, even when fewer matches remain. "
                + "These synthetic, unadjusted sale prices are not a current-market appraisal. The match index is not a confidence probability.");
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setEditable(false);
        note.setFocusable(false);
        note.setOpaque(false);
        note.setForeground(UiTheme.MUTED);
        note.setFont(note.getFont().deriveFont(12f));
        note.setRows(3);
        content.add(note, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel form = UiTheme.surface();
        form.setLayout(new BorderLayout(14, 0));
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 8);
        JLabel idLabel = UiTheme.muted("Property ID (required)");
        idLabel.setLabelFor(id);
        fields.add(idLabel, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        fields.add(id, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 15, 0, 8);
        JLabel countLabel = UiTheme.muted("Comparables (1–5)");
        countLabel.setLabelFor(comparableCount);
        fields.add(countLabel, c);
        c.gridx = 3;
        c.insets = new Insets(0, 0, 0, 0);
        comparableCount.setPreferredSize(new Dimension(77, 30));
        fields.add(comparableCount, c);
        form.add(fields, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        JButton clear = UiTheme.button("Clear", false);
        clear.setName("valuation.clear");
        clear.addActionListener(event -> clear());
        JButton calculate = UiTheme.button("Estimate value", true);
        calculate.setName("valuation.calculate");
        calculate.addActionListener(event -> estimate());
        id.addActionListener(event -> estimate());
        actions.add(clear);
        actions.add(calculate);
        form.add(actions, BorderLayout.EAST);
        return form;
    }

    private void estimate() {
        try {
            comparableCount.commitEdit();
            int count = ((Number) comparableCount.getValue()).intValue();
            Optional<ValuationResult> result = service.estimate(id.getText(), count);
            if (result.isEmpty()) {
                model.setComparables(java.util.List.of());
                estimate.setText("No comparables available");
                resultInfo.setText("No recent same-type comparable sales were found.");
                status.setForeground(UiTheme.MUTED);
                status.setText("Try another property ID; historical sales outside the three-year window are excluded.");
                return;
            }
            ValuationResult value = result.get();
            Property property = value.property();
            Neighborhood place = store.findNeighborhood(property.neighborhoodId());
            model.setComparables(value.comparables());
            estimate.setText(String.format(Locale.US, "%,.2f", value.estimatedPrice()));
            resultInfo.setText(property.id() + " · " + property.type() + " · "
                    + (place == null ? property.neighborhoodId() : place.name())
                    + " · " + value.comparables().size() + "/" + count + " recent comparable(s)");
            status.setForeground(UiTheme.MUTED);
            if (value.comparables().size() < count) {
                status.setText("Only " + value.comparables().size() + " recent comparable(s) available; older sales were not used.");
            } else {
                status.setText("Unadjusted average of the displayed recent sale prices. Clear resets the form.");
            }
        } catch (IllegalArgumentException | ParseException exception) {
            status.setForeground(UiTheme.ERROR);
            status.setText("Unable to estimate: " + exception.getMessage() + " Previous results are unchanged.");
        }
    }

    private void clear() {
        id.setText("");
        comparableCount.setValue(3);
        model.setComparables(java.util.List.of());
        estimate.setText("No estimate yet");
        resultInfo.setText("Enter an existing property ID to calculate an estimate.");
        status.setForeground(UiTheme.MUTED);
        status.setText("The system selects the closest same-type properties with recorded sales.");
    }
}
