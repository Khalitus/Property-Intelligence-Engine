package edu.shu.propertyintel.data;

import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvReader {
    private CsvReader() {
    }

    public static List<CsvRow> read(Path path) throws IOException {
        List<List<String>> records = parse(path);
        if (records.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty: " + path);
        }
        List<String> headers = new ArrayList<>(records.getFirst());
        headers.set(0, headers.getFirst().replace("\uFEFF", ""));
        List<CsvRow> result = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            if (records.get(i).size() != headers.size()) {
                throw new IllegalArgumentException("Invalid CSV column count in " + path
                        + " at record " + (i + 1));
            }
            result.add(new CsvRow(headers, records.get(i)));
        }
        return result;
    }

    private static List<List<String>> parse(Path path) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (PushbackReader reader = new PushbackReader(
                Files.newBufferedReader(path, StandardCharsets.UTF_8), 1)) {
            List<String> row = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            boolean started = false;
            int character;
            while ((character = reader.read()) != -1) {
                char c = (char) character;
                if (c == '"') {
                    if (quoted) {
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            quoted = false;
                            if (next != -1) {
                                reader.unread(next);
                            }
                        }
                    } else if (field.isEmpty()) {
                        quoted = true;
                    } else {
                        throw new IllegalArgumentException("Invalid quote in " + path);
                    }
                    started = true;
                } else if (!quoted && c == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    started = true;
                } else if (!quoted && (c == '\n' || c == '\r')) {
                    if (started || !row.isEmpty() || !field.isEmpty()) {
                        row.add(field.toString());
                        records.add(row);
                    }
                    row = new ArrayList<>();
                    field.setLength(0);
                    started = false;
                    if (c == '\r') {
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            reader.unread(next);
                        }
                    }
                } else {
                    field.append(c);
                    started = true;
                }
            }
            if (quoted) {
                throw new IllegalArgumentException("Unterminated CSV field in " + path);
            }
            if (started || !row.isEmpty() || !field.isEmpty()) {
                row.add(field.toString());
                records.add(row);
            }
        }
        return records;
    }

    public record CsvRow(List<String> headers, List<String> values) {
        public String get(String column) {
            int index = headers.indexOf(column);
            if (index < 0) {
                throw new IllegalArgumentException("Missing CSV column: " + column);
            }
            return values.get(index).trim();
        }
    }
}
