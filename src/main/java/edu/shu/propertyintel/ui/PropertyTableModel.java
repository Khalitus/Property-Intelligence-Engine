package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class PropertyTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Property ID", "Type", "Neighborhood", "Bedrooms", "Area (m²)", "Latest sale price", "Built"
    };

    private final PropertyStore store;
    private List<Property> rows = List.of();

    public PropertyTableModel(PropertyStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public void setProperties(List<Property> properties) {
        rows = List.copyOf(properties);
        fireTableDataChanged();
    }

    public Property propertyAt(int row) {
        return rows.get(row);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Property property = rows.get(row);
        Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
        Sale latest = store.latestSaleFor(property.id());
        return switch (column) {
            case 0 -> property.id();
            case 1 -> property.type();
            case 2 -> neighborhood == null ? "Unknown" : neighborhood.name();
            case 3 -> property.bedrooms();
            case 4 -> String.format(Locale.US, "%,.1f", property.areaSqm());
            case 5 -> latest == null ? "Not recorded" : String.format(Locale.US, "%,.2f", latest.price());
            case 6 -> property.yearBuilt();
            default -> throw new IndexOutOfBoundsException("Unknown column: " + column);
        };
    }
}
