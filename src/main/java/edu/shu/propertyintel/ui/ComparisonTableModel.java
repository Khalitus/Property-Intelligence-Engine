package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.service.BenchmarkResult;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Locale;

public final class ComparisonTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Operation", "Method A", "A (µs/op)", "Method B", "B (µs/op)", "Results"
    };
    private List<BenchmarkResult> benchmarks = List.of();

    public void setBenchmarks(List<BenchmarkResult> results) {
        benchmarks = List.copyOf(results);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return benchmarks.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }
    @Override public boolean isCellEditable(int row, int column) { return false; }

    @Override
    public Object getValueAt(int row, int column) {
        BenchmarkResult result = benchmarks.get(row);
        return switch (column) {
            case 0 -> result.operation();
            case 1 -> result.firstApproach();
            case 2 -> String.format(Locale.US, "%.3f", result.firstMicroseconds());
            case 3 -> result.secondApproach();
            case 4 -> String.format(Locale.US, "%.3f", result.secondMicroseconds());
            case 5 -> result.resultCount();
            default -> "";
        };
    }
}
