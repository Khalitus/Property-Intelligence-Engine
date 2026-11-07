package edu.shu.propertyintel.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void parsesCommasEscapedQuotesAndEmbeddedNewlines() throws IOException {
        Path input = tempDir.resolve("quoted.csv");
        Files.writeString(input, "id,description\r\nP0001,\"Bright, quiet \"\"family\"\" home\"\r\n"
                + "P0002,\"Line one\nLine two\"\n");
        List<CsvReader.CsvRow> rows = CsvReader.read(input);
        assertEquals(2, rows.size());
        assertEquals("Bright, quiet \"family\" home", rows.getFirst().get("description"));
        assertEquals("Line one\nLine two", rows.get(1).get("description"));
    }

    @Test
    void detectsMismatchedColumnCount() throws IOException {
        Path input = tempDir.resolve("bad.csv");
        Files.writeString(input, "id,name\nP0001\n");
        assertThrows(IllegalArgumentException.class, () -> CsvReader.read(input));
    }
}
