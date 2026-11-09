package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import edu.shu.propertyintel.service.PropertyRecommendation;
import edu.shu.propertyintel.service.RecommendationCriteria;
import edu.shu.propertyintel.service.RecommendationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public final class RecommendationPanel extends JPanel {
    private final PropertyStore store;
    private final RecommendationService service;
    private final RecommendationTableModel model;
    private final JTextField budget = new JTextField(12);
    private final JSpinner bedrooms = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
    private final JSpinner area = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 10));
    private final JComboBox<UiChoice<String>> type = new JComboBox<>();
    private final JComboBox<UiChoice<String>> neighborhood = new JComboBox<>();
    private final JLabel status = UiTheme.muted("Enter your requirements and select Find recommendations.");
    private final JTextArea explanation = new JTextArea();
    private final JButton historyButton = UiTheme.button("View sale history", true);
    private final JTable results;
    private final String defaultBudget;

    public RecommendationPanel(PropertyStore store, Consumer<Property> openHistory) {
        this.store = store;
        service = new RecommendationService(store);
        model = new RecommendationTableModel(store);
        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(26, 25, 25, 25));

        InputRules.price(budget);
        budget.setName("recommend.budget");
        bedrooms.setName("recommend.bedrooms");
        area.setName("recommend.area");
        type.setName("recommend.type");
        neighborhood.setName("recommend.neighborhood");
        UiTheme.combo(type, "Optional property-type restriction.");
        UiTheme.combo(neighborhood, "Optional neighborhood restriction.");
        bedrooms.setToolTipText("0 means any number of bedrooms; maximum 20.");
        area.setToolTipText("0 includes any area; measured in square meters.");
        initChoices();
        defaultBudget = recommendedInitialBudget();
        budget.setText(defaultBudget);

        JPanel top = new JPanel(new BorderLayout(0, 13));
        top.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        heading.add(UiTheme.heading("Property recommendations"), BorderLayout.NORTH);
        heading.add(UiTheme.muted("Filter historical property records and rank up to five matches using our Max Heap."), BorderLayout.SOUTH);
        top.add(heading, BorderLayout.NORTH);
        top.add(buildForm(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        results = new JTable(model);
        results.setName("recommend.results");
        results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {95, 95, 112, 62, 88, 116, 95, 104};
        for (int i = 0; i < widths.length; i++) {
            results.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        results.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateExplanation();
        });
        JPanel listPanel = UiTheme.surface();
        listPanel.setLayout(new BorderLayout(0, 10));
        listPanel.add(UiTheme.section("Top recommendations"), BorderLayout.NORTH);
        listPanel.add(UiTheme.tableScroll(results), BorderLayout.CENTER);
        listPanel.add(status, BorderLayout.SOUTH);
        listPanel.setMinimumSize(new Dimension(420, 230));

        JPanel detail = UiTheme.surface();
        detail.setLayout(new BorderLayout(0, 12));
        detail.add(UiTheme.section("Why this property?"), BorderLayout.NORTH);
        explanation.setEditable(false);
        explanation.setFocusable(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setOpaque(false);
        explanation.setForeground(UiTheme.INK);
        explanation.setFont(explanation.getFont().deriveFont(13f));
        JScrollPane explanationScroll = new JScrollPane(explanation);
        explanationScroll.setBorder(null);
        explanationScroll.setOpaque(false);
        explanationScroll.getViewport().setOpaque(false);
        detail.add(explanationScroll, BorderLayout.CENTER);
        historyButton.setName("recommend.history");
        historyButton.setEnabled(false);
        historyButton.addActionListener(e -> {
            int index = results.getSelectedRow();
            if (index >= 0) openHistory.accept(model.recommendationAt(results.convertRowIndexToModel(index)).property());
        });
        detail.add(historyButton, BorderLayout.SOUTH);
        detail.setMinimumSize(new Dimension(250, 230));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, detail);
        split.setResizeWeight(.69);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(split, BorderLayout.CENTER);
        javax.swing.SwingUtilities.invokeLater(() -> split.setDividerLocation(.69));
        calculate();
    }

    private void initChoices() {
        type.addItem(new UiChoice<>("Any type", ""));
        neighborhood.addItem(new UiChoice<>("Any neighborhood", ""));
        Set<String> types = new LinkedHashSet<>();
        Set<String> ids = new LinkedHashSet<>();
        for (Property property : store.allProperties()) {
            types.add(property.type());
            ids.add(property.neighborhoodId());
        }
        for (String value : types) type.addItem(new UiChoice<>(value, value));
        for (String id : ids) {
            Neighborhood place = store.findNeighborhood(id);
            if (place != null) neighborhood.addItem(new UiChoice<>(place.name(), id));
        }
    }

    private String recommendedInitialBudget() {
        double maximum = 0;
        for (Property property : store.allProperties()) {
            Sale sale = store.latestSaleFor(property.id());
            if (sale != null) maximum = Math.max(maximum, sale.price());
        }
        return String.format(Locale.US, "%.0f", maximum > 0 ? Math.ceil(maximum / 50000) * 50000 : 1000000);
    }

    private JPanel buildForm() {
        JPanel form = UiTheme.surface();
        form.setLayout(new BorderLayout(0, 10));
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addField(fields, 0, 0, "Maximum budget (required)", budget, 1.3);
        addField(fields, 1, 0, "Min. bedrooms (0 = any)", bedrooms, 1);
        addField(fields, 2, 0, "Min. area in m² (0 = any)", area, 1);
        addField(fields, 0, 1, "Property type", type, 1.3);
        addField(fields, 1, 1, "Neighborhood", neighborhood, 1);
        form.add(fields, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.add(UiTheme.muted("Score is a preference ranking (0–100), not a predicted sale probability."), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        JButton reset = UiTheme.button("Reset", false);
        reset.setName("recommend.reset");
        reset.addActionListener(e -> reset());
        JButton search = UiTheme.button("Find recommendations", true);
        search.setName("recommend.apply");
        search.addActionListener(e -> calculate());
        budget.addActionListener(e -> calculate());
        actions.add(reset);
        actions.add(search);
        bottom.add(actions, BorderLayout.EAST);
        form.add(bottom, BorderLayout.SOUTH);
        return form;
    }

    private static void addField(JPanel target, int column, int row, String title, JComponent component, double weight) {
        JPanel field = new JPanel(new BorderLayout(0, 5));
        field.setOpaque(false);
        JLabel label = UiTheme.muted(title);
        label.setFont(label.getFont().deriveFont(11f));
        label.setLabelFor(component);
        field.add(label, BorderLayout.NORTH);
        field.add(component, BorderLayout.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = column;
        c.gridy = row;
        c.weightx = weight;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(row == 0 ? 0 : 10, column == 0 ? 0 : 7, 0, 7);
        target.add(field, c);
    }

    private static String selected(JComboBox<UiChoice<String>> choices) {
        Object item = choices.getSelectedItem();
        return item instanceof UiChoice<?> choice ? String.valueOf(choice.value()) : "";
    }

    private void calculate() {
        try {
            bedrooms.commitEdit();
            area.commitEdit();
            Double maximum = InputRules.optionalPrice(budget.getText());
            if (maximum == null || maximum <= 0) {
                throw new IllegalArgumentException("Enter a maximum budget greater than zero.");
            }
            RecommendationCriteria criteria = new RecommendationCriteria(maximum,
                    ((Number) bedrooms.getValue()).intValue(), ((Number) area.getValue()).doubleValue(),
                    selected(type), selected(neighborhood));
            List<PropertyRecommendation> recommendations = service.recommend(criteria, 5);
            model.setRecommendations(recommendations);
            if (recommendations.isEmpty()) {
                results.clearSelection();
                explanation.setText("No properties meet all the selected limits. Increase the budget or relax another filter.");
                status.setText("No eligible properties; the previous results were cleared.");
            } else {
                results.setRowSelectionInterval(0, 0);
                status.setText(recommendations.size() + " recommendation(s), highest score first. Select a row for details.");
            }
            status.setForeground(UiTheme.MUTED);
            updateExplanation();
        } catch (IllegalArgumentException | ParseException exception) {
            status.setForeground(UiTheme.ERROR);
            status.setText("Check the inputs: " + exception.getMessage() + " Previous results are unchanged.");
        }
    }

    private void updateExplanation() {
        int selected = results.getSelectedRow();
        historyButton.setEnabled(selected >= 0);
        if (selected < 0) {
            if (model.getRowCount() == 0) explanation.setText("No recommendation selected. Relax your filters and search again.");
            return;
        }
        PropertyRecommendation item = model.recommendationAt(results.convertRowIndexToModel(selected));
        Neighborhood place = store.findNeighborhood(item.property().neighborhoodId());
        explanation.setText(String.format(Locale.US,
                "%s — %s%n%s%n%nRecorded sale: %,.2f on %s%nScore: %.1f / 100%nBedrooms: %d%nArea: %.1f m²%nParking: %d space(s)%n%n"
                + "Scoring weights:%nAffordability 25, schools 20, safety 20, transit 10, area 15, parking 10.%n%n"
                + "Only properties within your hard limits are ranked. These are historical transactions, not active listings. This is a rule-based score, not machine learning.",
                item.property().id(), item.property().type(), place == null ? "Unknown" : place.name(),
                item.price(), store.latestSaleFor(item.property().id()).date(), item.score(),
                item.property().bedrooms(), item.property().areaSqm(), item.property().parkingSpaces()));
        explanation.setCaretPosition(0);
    }

    private void reset() {
        budget.setText(defaultBudget);
        bedrooms.setValue(0);
        area.setValue(0);
        type.setSelectedIndex(0);
        neighborhood.setSelectedIndex(0);
        calculate();
    }
}
