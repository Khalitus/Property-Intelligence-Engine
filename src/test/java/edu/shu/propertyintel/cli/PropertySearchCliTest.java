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

class PropertySearchCliTest {
    @Test
    void searchesAndSortsUsingCustomQuickSort() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        store.addProperty(new Property("P01", "N01", "House", 3, 2, 180, 2010, 1, 0, false, "parking", "First"));
        store.addProperty(new Property("P02", "N01", "House", 4, 2, 200, 2015, 1, 0, false, "parking", "Second"));
        store.addSale(new Sale("S1", "P01", LocalDate.of(2025, 1, 1), 500000));
        store.addSale(new Sale("S2", "P02", LocalDate.of(2025, 1, 1), 600000));

        String commands = "5\nHouse\nMidtown\n3\n450000\n650000\n1\n2\n2\n10\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleApplication(store, new Scanner(commands), new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Matches: 2 | Sorted by PRICE (descending) using QUICK"));
        assertTrue(output.contains("\nP02") && output.contains("\nP01"));
        assertTrue(output.indexOf("\nP02") < output.indexOf("\nP01"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void rejectsInvalidSearchInputWithoutCrashing() {
        PropertyStore store = new PropertyStore();
        String commands = "5\n\n\n\n900\n10\n\n\n\n\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleApplication(store, new Scanner(commands), new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Invalid search input: Minimum price must not exceed maximum price"));
        assertTrue(output.contains("Goodbye."));
    }
}
