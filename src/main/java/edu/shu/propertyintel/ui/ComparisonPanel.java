package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.service.AlgorithmComparisonReport;
import edu.shu.propertyintel.service.AlgorithmComparisonService;
import edu.shu.propertyintel.service.PropertySearchService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public final class ComparisonPanel extends JPanel {
    private final AlgorithmComparisonService service;
    private final ComparisonTableModel model = new ComparisonTableModel();
    private final JLabel summary = UiTheme.section("No measurements yet");
    private final JLabel range = UiTheme.muted("Select Run comparison to measure the algorithms on your loaded dataset.");
    private final JLabel status = UiTheme.muted("Timings will be shown in microseconds per operation (µs/op).");
    private final JButton run = UiTheme.button("Run comparison", true);

    public ComparisonPanel(PropertyStore store, PropertySearchService search) {
        service = new AlgorithmComparisonService(store, search);
        setLayout(new BorderLayout(0, 15));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(26, 25, 25, 25));
        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        heading.add(UiTheme.heading("Algorithm comparison"), BorderLayout.NORTH);
        heading.add(UiTheme.muted("Compare our custom Hash Table, AVL Tree, Merge Sort and Quick Sort on the loaded dataset."), BorderLayout.SOUTH);
        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        JPanel controls = UiTheme.surface();
        controls.setLayout(new BorderLayout(12, 0));
        JPanel description = new JPanel(new BorderLayout(0, 6));
        description.setOpaque(false);
        description.add(UiTheme.section("Measured results"), BorderLayout.NORTH);
        description.add(UiTheme.muted("Runs verified, repeated operations with warm-up; results can change between runs."), BorderLayout.SOUTH);
        controls.add(description, BorderLayout.CENTER);
        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setOpaque(false);
        run.setName("comparison.run");
        run.addActionListener(event -> compare());
        action.add(run);
        controls.add(action, BorderLayout.EAST);
        top.add(controls, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel content = UiTheme.surface();
        content.setLayout(new BorderLayout(0, 14));
        JPanel overview = new JPanel(new BorderLayout(0, 6));
        overview.setOpaque(false);
        overview.add(summary, BorderLayout.NORTH);
        overview.add(range, BorderLayout.SOUTH);
        content.add(overview, BorderLayout.NORTH);
        JTable results = new JTable(model);
        results.setName("comparison.results");
        results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {184, 115, 105, 115, 105, 90};
        for (int i = 0; i < widths.length; i++) {
            results.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane scroll = UiTheme.tableScroll(results);
        scroll.setPreferredSize(new Dimension(600, 250));
        content.add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.NORTH);
        JTextArea explanation = new JTextArea("What is compared: property-ID lookup (Hash Table vs linear scan), "
                + "latest-sale price-range search (AVL Tree vs linear scan), and area sorting (Merge Sort vs Quick Sort). "
                + "Both approaches must agree on the results before timings are reported. "
                + "Microbenchmarks on small datasets are noisy: runtime figures are not speed guarantees and do not replace Big-O analysis.");
        explanation.setRows(3);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setEditable(false);
        explanation.setFocusable(false);
        explanation.setOpaque(false);
        explanation.setForeground(UiTheme.MUTED);
        bottom.add(explanation, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    private void compare() {
        run.setEnabled(false);
        status.setForeground(UiTheme.MUTED);
        status.setText("Measuring algorithms. The interface remains responsive.");
        SwingWorker<AlgorithmComparisonReport, Void> worker = new SwingWorker<>() {
            @Override
            protected AlgorithmComparisonReport doInBackground() {
                return service.compare();
            }

            @Override
            protected void done() {
                run.setEnabled(true);
                try {
                    AlgorithmComparisonReport report = get();
                    model.setBenchmarks(report.results());
                    summary.setText(report.propertyCount() + " properties · "
                            + report.pricedPropertyCount() + " with sale prices");
                    if (report.minimumRangePrice() == null) {
                        range.setText("No recorded sale prices; price-range comparison was skipped.");
                    } else {
                        range.setText(String.format(Locale.US,
                                "Inclusive price-range sample: %,.2f to %,.2f",
                                report.minimumRangePrice(), report.maximumRangePrice()));
                    }
                    status.setForeground(UiTheme.MUTED);
                    status.setText(report.results().size() + " algorithm comparisons completed. Re-run to see natural timing variation.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    status.setForeground(UiTheme.ERROR);
                    status.setText("Comparison interrupted. You can try again.");
                } catch (ExecutionException | CancellationException exception) {
                    status.setForeground(UiTheme.ERROR);
                    Throwable reason = exception.getCause() == null ? exception : exception.getCause();
                    status.setText("Comparison failed: " + reason.getMessage());
                }
            }
        };
        worker.execute();
    }
}
