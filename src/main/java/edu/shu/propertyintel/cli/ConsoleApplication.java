package edu.shu.propertyintel.cli;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

public final class ConsoleApplication {
    private final PropertyStore store;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleApplication(PropertyStore store, Scanner input, PrintStream output) {
        this.store = store;
        this.input = input;
        this.output = output;
    }

    public void run() {
        while (true) {
            output.println("\nPROPERTY INTELLIGENCE ENGINE - TASK 1");
            output.println("1. Dataset summary");
            output.println("2. Browse properties");
            output.println("3. Find property by ID");
            output.println("4. View sale history");
            output.println("0. Exit");
            String choice = read("Choose an option: ");
            if (choice == null || choice.equals("0")) {
                output.println("Goodbye.");
                return;
            }
            switch (choice) {
                case "1" -> summary();
                case "2" -> browse();
                case "3" -> findProperty();
                case "4" -> showHistory();
                default -> output.println("Invalid option. Choose 0-4.");
            }
        }
    }

    private void summary() {
        output.printf("Properties: %d%n", store.propertyCount());
        output.printf("Sales: %d%n", store.saleCount());
        output.printf("Neighborhoods: %d%n", store.neighborhoodCount());
    }

    private void browse() {
        String rawLimit = read("How many properties (1-50)? ");
        if (rawLimit == null) {
            return;
        }
        int limit;
        try {
            limit = Integer.parseInt(rawLimit);
        } catch (NumberFormatException exception) {
            output.println("Please enter a whole number.");
            return;
        }
        if (limit < 1 || limit > 50) {
            output.println("Please choose a number from 1 to 50.");
            return;
        }
        output.printf("%-8s %-12s %-14s %5s %10s %14s%n",
                "ID", "TYPE", "NEIGHBORHOOD", "BEDS", "AREA", "LATEST PRICE");
        int shown = 0;
        for (Property property : store.allProperties()) {
            if (shown++ == limit) {
                break;
            }
            Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
            Sale latest = store.latestSaleFor(property.id());
            output.printf(Locale.US, "%-8s %-12s %-14s %5d %10.1f %14s%n",
                    property.id(), property.type(), neighborhood.name(),
                    property.bedrooms(), property.areaSqm(),
                    latest == null ? "N/A" : String.format(Locale.US, "%,.0f", latest.price()));
        }
    }

    private void findProperty() {
        String id = read("Property ID: ");
        if (id == null) {
            return;
        }
        Property property = store.findProperty(id.toUpperCase(Locale.ROOT));
        if (property == null) {
            output.println("Property not found.");
            return;
        }
        Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
        Sale latest = store.latestSaleFor(property.id());
        output.printf("ID: %s%nType: %s%nNeighborhood: %s%nBedrooms: %d%nBathrooms: %d%n",
                property.id(), property.type(), neighborhood.name(), property.bedrooms(), property.bathrooms());
        output.printf(Locale.US, "Area: %.1f sqm%nYear built: %d%nParking spaces: %d%nFurnished: %s%n",
                property.areaSqm(), property.yearBuilt(), property.parkingSpaces(),
                property.furnished() ? "Yes" : "No");
        output.printf("Amenities: %s%nDescription: %s%n", property.amenities(), property.description());
        if (latest != null) {
            output.printf(Locale.US, "Latest recorded sale: %,.2f on %s%n",
                    latest.price(), latest.date());
        } else {
            output.println("Latest recorded sale: unavailable");
        }
    }

    private void showHistory() {
        String id = read("Property ID: ");
        if (id == null) {
            return;
        }
        String propertyId = id.toUpperCase(Locale.ROOT);
        if (store.findProperty(propertyId) == null) {
            output.println("Property not found.");
            return;
        }
        if (store.salesFor(propertyId).isEmpty()) {
            output.println("No sales recorded for this property.");
            return;
        }
        output.println("Sales for " + propertyId + " (oldest first):");
        Sale previous = null;
        for (Sale sale : store.salesFor(propertyId)) {
            output.printf(Locale.US, "%s  %,.2f", sale.date(), sale.price());
            if (previous != null) {
                double change = sale.price() - previous.price();
                output.printf(Locale.US, "  Change: %,+.2f", change);
                if (previous.price() != 0) {
                    output.printf(Locale.US, " (%+.2f%%)", change / previous.price() * 100);
                }
            }
            output.println();
            previous = sale;
        }
    }

    private String read(String prompt) {
        output.print(prompt);
        return input.hasNextLine() ? input.nextLine().trim() : null;
    }
}
