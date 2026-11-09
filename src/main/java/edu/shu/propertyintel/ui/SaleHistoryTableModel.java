package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.model.Sale;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SaleHistoryTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Sale date", "Recorded sale price", "Change from previous", "Change (%)"
    };
    private List<Row> rows = List.of();

    public void setSales(Iterable<Sale> sales) {
        List<Row> updated = new ArrayList<>();
        Sale previous = null;
        for (Sale sale : sales) {
            String priceChange = "—";
            String percentChange = "—";
            if (previous != null) {
                double difference = sale.price() - previous.price();
                priceChange = String.format(Locale.US, "%+,.2f", difference);
                if (previous.price() != 0) {
                    percentChange = String.format(Locale.US, "%+.2f%%", difference / previous.price() * 100);
                }
            }
            updated.add(new Row(sale.date().toString(),
                    String.format(Locale.US, "%,.2f", sale.price()), priceChange, percentChange));
            previous = sale;
        }
        rows = List.copyOf(updated);
        fireTableDataChanged();
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
        Row sale = rows.get(row);
        return switch (column) {
            case 0 -> sale.date();
            case 1 -> sale.price();
            case 2 -> sale.change();
            case 3 -> sale.percent();
            default -> throw new IndexOutOfBoundsException("Unknown column: " + column);
        };
    }

    private record Row(String date, String price, String change, String percent) {
    }
}
