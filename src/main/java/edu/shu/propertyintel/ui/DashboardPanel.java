package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import edu.shu.propertyintel.service.PropertySearchService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.Locale;

public final class DashboardPanel extends JPanel {
    public DashboardPanel(PropertyStore store, PropertySearchService search, Path dataPath,
                          Runnable browseAction) {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(28, 28, 28, 28));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        JPanel intro = new JPanel(new BorderLayout(0, 5));
        intro.setOpaque(false);
        intro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        intro.add(UiTheme.heading("Overview"), BorderLayout.NORTH);
        intro.add(UiTheme.muted("A summary of the property records loaded from your local CSV files."),
                BorderLayout.SOUTH);
        content.add(intro);
        content.add(Box.createVerticalStrut(23));

        JPanel statistics = new JPanel(new GridLayout(1, 4, 12, 0));
        statistics.setOpaque(false);
        statistics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        statistics.add(metric("Properties", store.propertyCount(), "Property records"));
        statistics.add(metric("Recorded sales", store.saleCount(), "Historical transactions"));
        statistics.add(metric("Neighborhoods", store.neighborhoodCount(), "Distinct locations"));
        statistics.add(metric("Price indexed", search.indexedPropertyCount(),
                "AVL height: " + search.priceIndexHeight()));
        content.add(statistics);
        content.add(Box.createVerticalStrut(22));

        int withSales = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Property property : store.allProperties()) {
            Sale sale = store.latestSaleFor(property.id());
            if (sale != null) {
                withSales++;
                min = Math.min(min, sale.price());
                max = Math.max(max, sale.price());
            }
        }

        JPanel summary = UiTheme.surface();
        summary.setLayout(new BorderLayout(0, 12));
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 158));
        summary.add(UiTheme.section("About these prices"), BorderLayout.NORTH);
        String range = withSales == 0 ? "No sales recorded" : String.format(Locale.US,
                "%,.2f to %,.2f", min, max);
        JTextArea summaryText = paragraph(
                "Latest recorded sale-price range: " + range + "\n"
                        + "Properties with sale records: " + withSales + " of " + store.propertyCount() + "\n"
                        + "Prices are dataset units, not current listing prices. The data is read-only.");
        summary.add(summaryText, BorderLayout.CENTER);
        content.add(summary);
        content.add(Box.createVerticalStrut(18));

        JPanel guidance = UiTheme.surface();
        guidance.setLayout(new BorderLayout(12, 12));
        guidance.setMaximumSize(new Dimension(Integer.MAX_VALUE, 206));
        guidance.add(UiTheme.section("Get started"), BorderLayout.NORTH);
        guidance.add(paragraph("Properties — filter by type, neighborhood, bedrooms or latest sale price; "
                + "choose Merge Sort or Quick Sort, then select a row for details.\n\n"
                + "History — enter a property ID or use ‘View sale history’ from a property detail.\n\n"
                + "Dataset directory: " + dataPath.toAbsolutePath().normalize()), BorderLayout.CENTER);
        JButton browse = UiTheme.button("Browse properties", true);
        browse.addActionListener(event -> browseAction.run());
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(browse, BorderLayout.WEST);
        guidance.add(actions, BorderLayout.SOUTH);
        content.add(guidance);
        content.add(Box.createVerticalGlue());

        JScrollPane scrolling = new JScrollPane(content);
        scrolling.setBorder(null);
        scrolling.getViewport().setBackground(UiTheme.BACKGROUND);
        scrolling.getVerticalScrollBar().setUnitIncrement(14);
        add(scrolling, BorderLayout.CENTER);
    }

    private static JPanel metric(String label, int value, String description) {
        JPanel card = UiTheme.surface();
        card.setLayout(new BorderLayout(0, 7));
        card.add(UiTheme.muted(label), BorderLayout.NORTH);
        JLabel total = new JLabel(String.format(Locale.US, "%,d", value));
        total.setFont(total.getFont().deriveFont(Font.BOLD, 30f));
        total.setForeground(UiTheme.INK);
        card.add(total, BorderLayout.CENTER);
        JLabel info = UiTheme.muted(description);
        info.setFont(info.getFont().deriveFont(11f));
        card.add(info, BorderLayout.SOUTH);
        return card;
    }

    private static JTextArea paragraph(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(area.getFont().deriveFont(13f));
        area.setForeground(UiTheme.INK);
        return area;
    }
}
