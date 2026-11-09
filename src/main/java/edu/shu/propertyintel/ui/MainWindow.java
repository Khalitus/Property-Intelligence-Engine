package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.service.PropertySearchService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Font;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MainWindow extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final Map<String, JButton> navigation = new LinkedHashMap<>();

    public MainWindow(PropertyStore store, Path dataDirectory) {
        super("Property Intelligence Engine");
        PropertySearchService search = new PropertySearchService(store);
        HistoryPanel historyPanel = new HistoryPanel(store);
        SearchPanel searchPanel = new SearchPanel(store, search, property -> {
            historyPanel.showProperty(property.id());
            open("History");
        });
        DashboardPanel dashboardPanel = new DashboardPanel(store, search, dataDirectory,
                () -> open("Properties"));
        RecommendationPanel recommendationPanel = new RecommendationPanel(store, property -> {
            historyPanel.showProperty(property.id());
            open("History");
        });
        ValuationPanel valuationPanel = new ValuationPanel(store);
        ComparisonPanel comparisonPanel = new ComparisonPanel(store, search);
        pages.add(dashboardPanel, "Dashboard");
        pages.add(searchPanel, "Properties");
        pages.add(historyPanel, "History");
        pages.add(recommendationPanel, "Recommendations");
        pages.add(valuationPanel, "Valuation");
        pages.add(comparisonPanel, "Algorithms");
        pages.setBackground(UiTheme.BACKGROUND);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UiTheme.BACKGROUND);
        main.add(sidebar(), BorderLayout.WEST);
        main.add(pages, BorderLayout.CENTER);
        setContentPane(main);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int availableWidth = Math.max(900, usable.width - 20);
        int availableHeight = Math.max(600, usable.height - 20);
        setMinimumSize(new Dimension(Math.min(1000, availableWidth), Math.min(650, availableHeight)));
        setSize(Math.min(1260, availableWidth), Math.min(820, availableHeight));
        setLocationRelativeTo(null);
        open("Dashboard");
    }

    private JPanel sidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(202, 0));
        side.setBackground(UiTheme.SURFACE);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UiTheme.BORDER),
                UiTheme.padding(28, 14, 18, 14)));

        JLabel brand = new JLabel("PROPERTY");
        brand.setForeground(UiTheme.ACCENT);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        side.add(brand);
        JLabel sub = UiTheme.muted("INTELLIGENCE ENGINE");
        sub.setFont(sub.getFont().deriveFont(Font.BOLD, 10f));
        sub.setAlignmentX(LEFT_ALIGNMENT);
        side.add(sub);
        side.add(Box.createVerticalStrut(28));

        for (String page : new String[]{"Dashboard", "Properties", "History", "Recommendations", "Valuation", "Algorithms"}) {
            JButton button = UiTheme.navigationButton(page);
            button.setName("nav." + page.toLowerCase());
            button.setHorizontalAlignment(JButton.LEFT);
            button.setFocusPainted(false);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addActionListener(event -> open(page));
            navigation.put(page, button);
            side.add(button);
            side.add(Box.createVerticalStrut(7));
        }
        side.add(Box.createVerticalGlue());
        JLabel foot = UiTheme.muted("Local CSV · Java 21");
        foot.setFont(foot.getFont().deriveFont(11f));
        foot.setAlignmentX(LEFT_ALIGNMENT);
        side.add(foot);
        return side;
    }

    private void open(String page) {
        cards.show(pages, page);
        for (Map.Entry<String, JButton> entry : navigation.entrySet()) {
            boolean selected = entry.getKey().equals(page);
            JButton button = entry.getValue();
            UiTheme.selectNavigation(button, selected);
            button.setFont(button.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN, 13f));
        }
    }
}
