package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.service.PropertyRecommendation;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Locale;

public final class RecommendationTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Property ID", "Type", "Neighborhood", "Beds", "Area (m²)", "Sale price", "Score / 100"
    };
    private final PropertyStore store;
    private List<PropertyRecommendation> recommendations = List.of();

    public RecommendationTableModel(PropertyStore store) {
        this.store = store;
    }

    public void setRecommendations(List<PropertyRecommendation> results) {
        recommendations = List.copyOf(results);
        fireTableDataChanged();
    }

    public PropertyRecommendation recommendationAt(int row) {
        return recommendations.get(row);
    }

    @Override public int getRowCount() { return recommendations.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }
    @Override public boolean isCellEditable(int row, int column) { return false; }

    @Override
    public Object getValueAt(int row, int column) {
        PropertyRecommendation result = recommendations.get(row);
        Neighborhood neighborhood = store.findNeighborhood(result.property().neighborhoodId());
        return switch (column) {
            case 0 -> result.property().id();
            case 1 -> result.property().type();
            case 2 -> neighborhood == null ? result.property().neighborhoodId() : neighborhood.name();
            case 3 -> result.property().bedrooms();
            case 4 -> String.format(Locale.US, "%.1f", result.property().areaSqm());
            case 5 -> String.format(Locale.US, "%,.2f", result.price());
            case 6 -> String.format(Locale.US, "%.1f", result.score());
            default -> "";
        };
    }
}
