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

class PropertyIntelligenceCliTest {
    @Test
    void exposesRecommendationAndValuationMenus() {
        PropertyStore store = fixture();
        String text = run(store, "6\n600000\n3\n100\nHouse\n\n7\nP01\n0\n");
        assertTrue(text.contains("TOP RECOMMENDATIONS"));
        assertTrue(text.contains("P01"));
        assertTrue(text.contains("Estimated value of P01: 500,000.00"));
        assertTrue(text.contains("Goodbye."));
    }

    @Test
    void reportsInvalidInputAndMissingComparablesWithoutCrashing() {
        PropertyStore store = fixture();
        String text = run(store, "6\n-5\n0\n0\n\n\n7\nNOPE\n0\n");
        assertTrue(text.contains("Invalid recommendation input:"));
        assertTrue(text.contains("Unable to estimate value: Property not found"));
        assertTrue(text.contains("Goodbye."));
    }

    private PropertyStore fixture() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 9, 8, 8));
        store.addProperty(new Property("P01", "N01", "House", 3, 2, 180, 2010, 1, 1, true, "parking", "A"));
        store.addProperty(new Property("P02", "N01", "House", 3, 2, 182, 2011, 1, 1, true, "parking", "B"));
        store.addSale(new Sale("S01", "P01", LocalDate.of(2025, 1, 1), 400_000));
        store.addSale(new Sale("S02", "P02", LocalDate.of(2025, 1, 1), 500_000));
        return store;
    }

    private String run(PropertyStore store, String commands) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleApplication(store, new Scanner(commands),
                new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
