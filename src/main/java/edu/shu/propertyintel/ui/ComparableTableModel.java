package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.service.ComparableProperty;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Locale;

public final class ComparableTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Property ID", "Neighborhood", "Bedrooms", "Area (m²)", "Sale price", "Match index", "Sale date"
    };
    private final PropertyStore store;
    private List<ComparableProperty> comparables = List.of();

    public ComparableTableModel(PropertyStore store) { this.store = store; }

    public void setComparables(List<ComparableProperty> results) {
        comparables = List.copyOf(results);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return comparables.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }
    @Override public boolean isCellEditable(int row, int column) { return false; }

    @Override
    public Object getValueAt(int row, int column) {
        ComparableProperty comparable = comparables.get(row);
        Neighborhood neighborhood = store.findNeighborhood(comparable.property().neighborhoodId());
        return switch (column) {
            case 0 -> comparable.property().id();
            case 1 -> neighborhood == null ? comparable.property().neighborhoodId() : neighborhood.name();
            case 2 -> comparable.property().bedrooms();
            case 3 -> String.format(Locale.US, "%.1f", comparable.property().areaSqm());
            case 4 -> String.format(Locale.US, "%,.2f", comparable.latestPrice());
            case 5 -> String.format(Locale.US, "%.3f", comparable.similarity());
            case 6 -> store.latestSaleFor(comparable.property().id()).date().toString();
            default -> "";
        };
    }
}
