package edu.shu.propertyintel.cli;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CompleteCliTest {
    @Test
    void executesEveryMenuOptionInOneSession() {
        String inputs = "1\n2\n2\n3\nP01\n4\nP01\n"
                + "5\nHouse\n\n3\n200000\n800000\n1\n2\n2\n10\n"
                + "6\n900000\n3\n100\nHouse\n\n"
                + "7\nP01\n8\n0\n";
        String output = run(fixture(), inputs);
        assertTrue(output.contains("Properties: 3"));
        assertTrue(output.contains("ID: P01"));
        assertTrue(output.contains("Sales for P01"));
        assertTrue(output.contains("Matches: 2"));
        assertTrue(output.contains("TOP RECOMMENDATIONS"));
        assertTrue(output.contains("Estimated value of P01"));
        assertTrue(output.contains("ALGORITHM COMPARISON"));
        assertTrue(output.contains("Merge Sort"));
        assertTrue(output.contains("Quick Sort"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void acceptsInvalidOptionsAndReturnsToMenu() {
        String output = run(fixture(), "abc\n99\n8\n0\n");
        assertTrue(output.contains("Invalid option. Choose 0-8."));
        assertTrue(output.contains("ALGORITHM COMPARISON"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void handlesEmptyStoreInComparisonAndEndOfInput() {
        String output = run(new PropertyStore(), "8\n");
        assertTrue(output.contains("No property records available for comparison."));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void handlesMissingLookupAndNoMatchingSearch() {
        String inputs = "3\nMISSING\n5\nVilla\n\n\n\n\n\n\n\n\n0\n";
        String output = run(fixture(), inputs);
        assertTrue(output.contains("Property not found."));
        assertTrue(output.contains("No properties matched your filters."));
        assertTrue(output.contains("Goodbye."));
    }

    private static PropertyStore fixture() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N1", "North", 2, 9, 8, 8));
        store.addProperty(new Property("P01", "N1", "House", 3, 2, 170, 2010,
                1, 0, true, "balcony", "First"));
        store.addProperty(new Property("P02", "N1", "House", 4, 3, 210, 2015,
                2, 0, false, "parking", "Second"));
        store.addProperty(new Property("P03", "N1", "Apartment", 2, 1, 80, 2008,
                0, 2, false, "elevator", "Third"));
        store.addSale(new Sale("S1", "P01", LocalDate.of(2024, 2, 1), 350_000));
        store.addSale(new Sale("S2", "P01", LocalDate.of(2025, 2, 1), 400_000));
        store.addSale(new Sale("S3", "P02", LocalDate.of(2025, 2, 1), 550_000));
        store.addSale(new Sale("S4", "P03", LocalDate.of(2025, 2, 1), 250_000));
        return store;
    }

    private static String run(PropertyStore store, String commands) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new ConsoleApplication(store, new Scanner(commands),
                new PrintStream(buffer, true, StandardCharsets.UTF_8)).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
