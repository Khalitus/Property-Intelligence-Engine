package edu.shu.propertyintel.cli;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import edu.shu.propertyintel.service.PropertySearchService;
import edu.shu.propertyintel.service.SearchCriteria;
import edu.shu.propertyintel.service.SortAlgorithm;
import edu.shu.propertyintel.service.SortField;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class ConsoleApplication {
    private final PropertyStore store;
    private final PropertySearchService searchService;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleApplication(PropertyStore store, Scanner input, PrintStream output) {
        this.store = store;
        this.searchService = new PropertySearchService(store);
        this.input = input;
        this.output = output;
    }

    public void run() {
        while (true) {
            output.println("\nPROPERTY INTELLIGENCE ENGINE - TASK 2");
            output.println("1. Dataset summary");
            output.println("2. Browse properties");
            output.println("3. Find property by ID");
            output.println("4. View sale history");
            output.println("5. Search, filter and sort properties");
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
                case "5" -> searchProperties();
                default -> output.println("Invalid option. Choose 0-5.");
            }
        }
    }

    private void summary() {
        output.printf("Properties: %d%n", store.propertyCount());
        output.printf("Sales: %d%n", store.saleCount());
        output.printf("Neighborhoods: %d%n", store.neighborhoodCount());
        output.printf("Properties with recorded sales (AVL price index): %d%n", searchService.indexedPropertyCount());
        output.printf("AVL tree height: %d%n", searchService.priceIndexHeight());
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
        printHeader();
        int shown = 0;
        for (Property property : store.allProperties()) {
            if (shown >= limit) {
                break;
            }
            printPropertyRow(property);
            shown++;
        }
    }

    private void searchProperties() {
        output.println("Leave any filter blank to include all values.");
        String type = read("Property type (e.g. House): ");
        String neighborhood = read("Neighborhood name or ID: ");
        String beds = read("Minimum bedrooms: ");
        String minimum = read("Minimum latest sale price: ");
        String maximum = read("Maximum latest sale price: ");
        if (type == null || neighborhood == null || beds == null || minimum == null || maximum == null) {
            return;
        }
        String field = read("Sort by: 1=Price, 2=Area, 3=Bedrooms, 4=Year built [1]: ");
        String algorithm = read("Algorithm: 1=Merge Sort, 2=Quick Sort [1]: ");
        String direction = read("Order: 1=Ascending, 2=Descending [1]: ");
        String count = read("Show up to how many results (1-50) [20]: ");
        if (field == null || algorithm == null || direction == null || count == null) {
            return;
        }
        try {
            SearchCriteria criteria = new SearchCriteria(
                    type, neighborhood, optionalInteger(beds), optionalDouble(minimum), optionalDouble(maximum));
            SortField sortField = switch (field.isBlank() ? "1" : field) {
                case "1" -> SortField.PRICE;
                case "2" -> SortField.AREA;
                case "3" -> SortField.BEDROOMS;
                case "4" -> SortField.YEAR_BUILT;
                default -> throw new IllegalArgumentException("Choose a sort field from 1-4");
            };
            SortAlgorithm sortAlgorithm = switch (algorithm.isBlank() ? "1" : algorithm) {
                case "1" -> SortAlgorithm.MERGE;
                case "2" -> SortAlgorithm.QUICK;
                default -> throw new IllegalArgumentException("Choose a sorting algorithm from 1-2");
            };
            boolean ascending = switch (direction.isBlank() ? "1" : direction) {
                case "1" -> true;
                case "2" -> false;
                default -> throw new IllegalArgumentException("Choose an order from 1-2");
            };
            int limit = count.isBlank() ? 20 : Integer.parseInt(count);
            if (limit < 1 || limit > 50) {
                throw new IllegalArgumentException("Result limit must be between 1 and 50");
            }
            List<Property> matches = searchService.search(criteria, sortField, sortAlgorithm, ascending);
            output.printf("Matches: %d | Sorted by %s (%s) using %s%n", matches.size(),
                    sortField, ascending ? "ascending" : "descending", sortAlgorithm);
            if (matches.isEmpty()) {
                output.println("No properties matched your filters.");
                return;
            }
            printHeader();
            for (int i = 0; i < Math.min(limit, matches.size()); i++) {
                printPropertyRow(matches.get(i));
            }
            if (matches.size() > limit) {
                output.printf("Showing first %d of %d matches.%n", limit, matches.size());
            }
        } catch (IllegalArgumentException exception) {
            output.println("Invalid search input: " + exception.getMessage());
        }
    }

    private static Integer optionalInteger(String value) {
        return value.isBlank() ? null : Integer.valueOf(value);
    }

    private static Double optionalDouble(String value) {
        return value.isBlank() ? null : Double.valueOf(value);
    }

    private void printHeader() {
        output.printf("%-8s %-12s %-14s %5s %10s %14s%n",
                "ID", "TYPE", "NEIGHBORHOOD", "BEDS", "AREA", "LATEST PRICE");
    }

    private void printPropertyRow(Property property) {
        Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
        Sale latest = store.latestSaleFor(property.id());
        output.printf(Locale.US, "%-8s %-12s %-14s %5d %10.1f %14s%n",
                property.id(), property.type(), neighborhood.name(), property.bedrooms(), property.areaSqm(),
                latest == null ? "N/A" : String.format(Locale.US, "%,.0f", latest.price()));
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
            output.printf(Locale.US, "Latest recorded sale: %,.2f on %s%n", latest.price(), latest.date());
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
