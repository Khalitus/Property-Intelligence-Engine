package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.service.PropertySearchService;
import edu.shu.propertyintel.service.SearchCriteria;
import edu.shu.propertyintel.service.SortAlgorithm;
import edu.shu.propertyintel.service.SortField;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public final class SearchPanel extends JPanel {
    private final PropertySearchService service;
    private final PropertyStore store;
    private final PropertyTableModel tableModel;
    private final PropertyDetailsPanel details;

    private final JComboBox<Choice<String>> typeChoice = new JComboBox<>();
    private final JComboBox<Choice<String>> neighborhoodChoice = new JComboBox<>();
    private final JSpinner minimumBedrooms = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
    private final JTextField minimumPrice = new JTextField();
    private final JTextField maximumPrice = new JTextField();
    private final JTextField exactPropertyId = new JTextField(8);
    private final JComboBox<Choice<SortField>> sortChoice = new JComboBox<>();
    private final JComboBox<Choice<SortAlgorithm>> algorithmChoice = new JComboBox<>();
    private final JComboBox<Choice<Boolean>> directionChoice = new JComboBox<>();
    private final JLabel status = UiTheme.muted("Select Search to apply your filters.");
    private final JTable table;

    public SearchPanel(PropertyStore store, PropertySearchService service,
                       Consumer<Property> historyAction) {
        this.service = service;
        this.store = store;
        this.tableModel = new PropertyTableModel(store);
        this.details = new PropertyDetailsPanel(store, historyAction);
        setLayout(new BorderLayout(0, 14));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(26, 25, 25, 25));

        initializeChoices(store);
        InputRules.price(minimumPrice);
        InputRules.price(maximumPrice);
        InputRules.propertyId(exactPropertyId);
        exactPropertyId.setColumns(8);
        exactPropertyId.setName("search.exactId");
        exactPropertyId.addActionListener(event -> findById());
        minimumPrice.setName("search.minimumPrice");
        maximumPrice.setName("search.maximumPrice");
        minimumBedrooms.setName("search.minimumBedrooms");
        minimumBedrooms.setToolTipText("Choose at least this many bedrooms (0 includes all).");
        UiTheme.combo(typeChoice, "All types includes every property type in the dataset.");
        UiTheme.combo(neighborhoodChoice, "All neighborhoods includes every location.");
        UiTheme.combo(sortChoice, "Choose which property attribute determines result order.");
        UiTheme.combo(algorithmChoice, "Use our own Merge Sort or Quick Sort implementation.");
        UiTheme.combo(directionChoice, "Select ascending or descending result order.");

        JPanel headerAndFilters = new JPanel(new BorderLayout(0, 17));
        headerAndFilters.setOpaque(false);
        JPanel introduction = new JPanel(new BorderLayout(0, 4));
        introduction.setOpaque(false);
        introduction.add(UiTheme.heading("Find properties"), BorderLayout.NORTH);
        introduction.add(UiTheme.muted(
                "Choose filters, select a sorting algorithm, then click Search. Blank prices mean no limit."),
                BorderLayout.SOUTH);
        headerAndFilters.add(introduction, BorderLayout.NORTH);
        headerAndFilters.add(createFilters(), BorderLayout.CENTER);
        add(headerAndFilters, BorderLayout.NORTH);

        table = new JTable(tableModel);
        table.setName("search.results");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getAccessibleContext().setAccessibleDescription(
                "Search results ordered by the selected custom sorting algorithm; select a row to view details.");
        int[] widths = {76, 96, 106, 76, 90, 128, 60};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                details.showProperty(row < 0 ? null : tableModel.propertyAt(table.convertRowIndexToModel(row)));
            }
        });

        JPanel resultsPanel = new JPanel(new BorderLayout(0, 11));
        resultsPanel.setBackground(UiTheme.SURFACE);
        resultsPanel.setBorder(UiTheme.padding(15, 15, 15, 15));
        JPanel resultsHeader = new JPanel(new BorderLayout(7, 0));
        resultsHeader.setOpaque(false);
        resultsHeader.add(UiTheme.section("Search results"), BorderLayout.WEST);
        JPanel quickLookup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        quickLookup.setOpaque(false);
        JLabel idLabel = UiTheme.muted("Find by ID");
        idLabel.setLabelFor(exactPropertyId);
        quickLookup.add(idLabel);
        quickLookup.add(exactPropertyId);
        JButton findIdButton = UiTheme.button("Find", false);
        findIdButton.setName("search.findId");
        findIdButton.setToolTipText("Look up an exact ID using the custom hash table; ignores other filters.");
        findIdButton.addActionListener(event -> findById());
        quickLookup.add(findIdButton);
        resultsHeader.add(quickLookup, BorderLayout.EAST);
        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        JScrollPane tableScroll = UiTheme.tableScroll(table);
        tableScroll.getHorizontalScrollBar().setUnitIncrement(18);
        resultsPanel.add(tableScroll, BorderLayout.CENTER);
        resultsPanel.add(status, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsPanel, details);
        split.setName("search.split");
        split.setContinuousLayout(true);
        split.setResizeWeight(.70);
        split.setDividerSize(8);
        split.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        resultsPanel.setMinimumSize(new Dimension(430, 280));
        details.setMinimumSize(new Dimension(270, 280));
        add(split, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(.70));

        minimumPrice.addActionListener(event -> executeSearch());
        maximumPrice.addActionListener(event -> executeSearch());
        executeSearch();
    }

    private void initializeChoices(PropertyStore store) {
        typeChoice.addItem(new Choice<>("All types", ""));
        neighborhoodChoice.addItem(new Choice<>("All neighborhoods", ""));
        Set<String> types = new LinkedHashSet<>();
        Set<String> neighborhoodIds = new LinkedHashSet<>();
        for (Property property : store.allProperties()) {
            types.add(property.type());
            neighborhoodIds.add(property.neighborhoodId());
        }
        for (String type : types) {
            typeChoice.addItem(new Choice<>(type, type));
        }
        for (String id : neighborhoodIds) {
            Neighborhood neighborhood = store.findNeighborhood(id);
            if (neighborhood != null) {
                neighborhoodChoice.addItem(new Choice<>(neighborhood.name(), id));
            }
        }
        sortChoice.addItem(new Choice<>("Latest sale price", SortField.PRICE));
        sortChoice.addItem(new Choice<>("Area", SortField.AREA));
        sortChoice.addItem(new Choice<>("Bedrooms", SortField.BEDROOMS));
        sortChoice.addItem(new Choice<>("Year built", SortField.YEAR_BUILT));
        algorithmChoice.addItem(new Choice<>("Merge Sort", SortAlgorithm.MERGE));
        algorithmChoice.addItem(new Choice<>("Quick Sort", SortAlgorithm.QUICK));
        directionChoice.addItem(new Choice<>("Ascending", Boolean.TRUE));
        directionChoice.addItem(new Choice<>("Descending", Boolean.FALSE));
    }

    private JPanel createFilters() {
        JPanel form = UiTheme.surface();
        form.setLayout(new BorderLayout(0, 12));
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        addField(fields, 0, 0, 1.3, "Property type", typeChoice);
        addField(fields, 1, 0, 1.5, "Neighborhood", neighborhoodChoice);
        addField(fields, 2, 0, .7, "Min. bedrooms (0 = any)", minimumBedrooms);
        addField(fields, 3, 0, 1.1, "Min. sale price", minimumPrice);
        addField(fields, 4, 0, 1.1, "Max. sale price", maximumPrice);
        addField(fields, 0, 1, 1.3, "Sort by", sortChoice);
        addField(fields, 1, 1, 1.5, "Sorting algorithm", algorithmChoice);
        addField(fields, 2, 1, .7, "Order", directionChoice);

        JPanel actions = new JPanel(new GridLayout(1, 2, 7, 0));
        actions.setOpaque(false);
        JButton reset = UiTheme.button("Reset", false);
        reset.setName("search.reset");
        reset.setToolTipText("Clear all filters and restore the default sorting options.");
        reset.addActionListener(event -> resetFilters());
        JButton search = UiTheme.button("Search", true);
        search.setName("search.apply");
        search.setToolTipText("Run property filtering and sorting using the selected custom algorithms.");
        search.addActionListener(event -> executeSearch());
        actions.add(reset);
        actions.add(search);
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridx = 3;
        constraint.gridy = 1;
        constraint.gridwidth = 2;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 2.2;
        constraint.insets = new Insets(6, 5, 0, 0);
        fields.add(actions, constraint);
        form.add(fields, BorderLayout.CENTER);
        return form;
    }

    private static void addField(JPanel target, int column, int row, double weight,
                                 String title, JComponent input) {
        JPanel holder = new JPanel(new BorderLayout(0, 5));
        holder.setOpaque(false);
        JLabel label = UiTheme.muted(title);
        label.setFont(label.getFont().deriveFont(11f));
        label.setLabelFor(input);
        holder.add(label, BorderLayout.NORTH);
        holder.add(input, BorderLayout.CENTER);
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridx = column;
        constraint.gridy = row;
        constraint.weightx = weight;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.insets = new Insets(row == 0 ? 0 : 8, column == 0 ? 0 : 5, 0, column == 4 ? 0 : 5);
        target.add(holder, constraint);
    }

    private void resetFilters() {
        typeChoice.setSelectedIndex(0);
        neighborhoodChoice.setSelectedIndex(0);
        minimumBedrooms.setValue(0);
        minimumPrice.setText("");
        maximumPrice.setText("");
        exactPropertyId.setText("");
        sortChoice.setSelectedIndex(0);
        algorithmChoice.setSelectedIndex(0);
        directionChoice.setSelectedIndex(0);
        executeSearch();
    }

    private void findById() {
        String id = exactPropertyId.getText().trim().toUpperCase(Locale.ROOT);
        if (id.isBlank()) {
            status.setForeground(UiTheme.ERROR);
            status.setText("Enter a property ID, such as P0001, before selecting Find.");
            return;
        }
        Property property = store.findProperty(id);
        if (property == null) {
            status.setForeground(UiTheme.ERROR);
            status.setText("No property exists with ID " + id + ". Existing results are unchanged.");
            return;
        }
        tableModel.setProperties(List.of(property));
        table.setRowSelectionInterval(0, 0);
        details.showProperty(property);
        status.setForeground(UiTheme.MUTED);
        status.setText("Exact ID match: " + id + " · Other filters not applied. Select Reset to view all.");
    }

    private void executeSearch() {
        try {
            minimumBedrooms.commitEdit();
            int bedrooms = (Integer) minimumBedrooms.getValue();
            Double min = InputRules.optionalPrice(minimumPrice.getText());
            Double max = InputRules.optionalPrice(maximumPrice.getText());
            SearchCriteria criteria = new SearchCriteria(
                    selected(typeChoice), selected(neighborhoodChoice), bedrooms, min, max);
            SortField sortField = selected(sortChoice);
            SortAlgorithm algorithm = selected(algorithmChoice);
            boolean ascending = selected(directionChoice);
            List<Property> result = service.search(criteria, sortField, algorithm, ascending);
            tableModel.setProperties(result);
            if (result.isEmpty()) {
                table.clearSelection();
                details.showProperty(null);
                status.setText("No properties matched. Try resetting or relaxing your filters.");
            } else {
                table.setRowSelectionInterval(0, 0);
                details.showProperty(result.getFirst());
                status.setText(result.size() + (result.size() == 1 ? " property" : " properties")
                        + " · " + selectedLabel(sortChoice) + " · " + selectedLabel(algorithmChoice)
                        + " · " + selectedLabel(directionChoice));
            }
            status.setForeground(UiTheme.MUTED);
        } catch (ParseException | IllegalArgumentException exception) {
            status.setForeground(UiTheme.ERROR);
            status.setText("Check your filters: " + exception.getMessage());
        }
    }

    private static <T> T selected(JComboBox<Choice<T>> box) {
        @SuppressWarnings("unchecked")
        Choice<T> choice = (Choice<T>) box.getSelectedItem();
        return choice.value();
    }

    private static <T> String selectedLabel(JComboBox<Choice<T>> box) {
        return box.getSelectedItem().toString();
    }

    private record Choice<T>(String label, T value) {
        @Override
        public String toString() {
            return label;
        }
    }
}
