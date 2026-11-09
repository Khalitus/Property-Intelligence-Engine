package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;
import java.util.function.Consumer;

public final class PropertyDetailsPanel extends JPanel {
    private final PropertyStore store;
    private final Consumer<Property> historyAction;
    private final JLabel propertyHeader = UiTheme.muted("Select a property in the table.");
    private final JTextArea description = new JTextArea();
    private final JButton historyButton = UiTheme.button("View sale history", true);
    private Property selected;

    public PropertyDetailsPanel(PropertyStore store, Consumer<Property> historyAction) {
        this.store = store;
        this.historyAction = historyAction;
        setLayout(new BorderLayout(0, 12));
        setBackground(UiTheme.SURFACE);
        setBorder(UiTheme.padding(17, 16, 17, 16));
        JPanel title = new JPanel(new BorderLayout(0, 6));
        title.setOpaque(false);
        title.add(UiTheme.section("Property details"), BorderLayout.NORTH);
        title.add(propertyHeader, BorderLayout.SOUTH);
        add(title, BorderLayout.NORTH);

        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setForeground(UiTheme.INK);
        description.setFont(description.getFont().deriveFont(Font.PLAIN, 13f));
        description.setText("Choose any row to see its complete property information here.");
        JScrollPane scroll = new JScrollPane(description);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiTheme.SURFACE);
        add(scroll, BorderLayout.CENTER);

        historyButton.setEnabled(false);
        historyButton.setToolTipText("Open the historical sales for the selected property.");
        historyButton.addActionListener(event -> {
            if (selected != null) {
                historyAction.accept(selected);
            }
        });
        add(historyButton, BorderLayout.SOUTH);
    }

    public void showProperty(Property property) {
        selected = property;
        historyButton.setEnabled(property != null);
        if (property == null) {
            propertyHeader.setText("No property selected");
            description.setText("No matching property is selected. Change your filters or choose a row.");
            return;
        }
        propertyHeader.setText(property.id() + " · " + property.type());
        Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
        Sale sale = store.latestSaleFor(property.id());
        String amenities = property.amenities() == null || property.amenities().isBlank()
                ? "Not specified" : property.amenities().replace("|", ", ");
        String details = "Neighborhood: " + (neighborhood == null ? "Unknown" : neighborhood.name())
                + "\n\nBedrooms: " + property.bedrooms()
                + "\nBathrooms: " + property.bathrooms()
                + String.format(Locale.US, "\nArea: %,.1f m²", property.areaSqm())
                + "\nYear built: " + property.yearBuilt()
                + "\nFloor: " + property.floor()
                + "\nParking spaces: " + property.parkingSpaces()
                + "\nFurnished: " + (property.furnished() ? "Yes" : "No")
                + "\n\nLatest recorded sale: "
                + (sale == null ? "Not recorded" : String.format(Locale.US, "%,.2f on %s", sale.price(), sale.date()))
                + "\n\nAmenities: " + amenities
                + "\n\nDescription:\n" + property.description();
        description.setText(details);
        description.setCaretPosition(0);
    }
}
