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

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleApplicationTest {
    @Test
    void supportsSummaryLookupHistoryAndCleanExit() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        store.addProperty(new Property("P0001", "N01", "House", 3, 2, 180, 2010,
                1, 0, false, "parking", "Family house"));
        store.addSale(new Sale("S01", "P0001", LocalDate.of(2024, 1, 1), 350000));
        store.addSale(new Sale("S02", "P0001", LocalDate.of(2025, 1, 1), 400000));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        String commands = "1\n3\np0001\n4\nP0001\n2\n1\n0\n";
        new ConsoleApplication(store, new Scanner(commands), output).run();
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Properties: 1"));
        assertTrue(text.contains("ID: P0001"));
        assertTrue(text.contains("+50,000.00"));
        assertTrue(text.contains("Goodbye."));
    }
}
