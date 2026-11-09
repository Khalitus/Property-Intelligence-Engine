package edu.shu.propertyintel.data;

import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

public final class DatasetLoader {
    public PropertyStore load(Path directory) throws IOException {
        PropertyStore store = new PropertyStore();
        for (CsvReader.CsvRow row : CsvReader.read(directory.resolve("neighborhoods.csv"))) {
            store.addNeighborhood(new Neighborhood(
                    row.get("neighborhood_id"),
                    row.get("neighborhood_name"),
                    number(row, "distance_to_center_km"),
                    number(row, "school_score"),
                    number(row, "transit_score"),
                    number(row, "safety_score")));
        }
        for (CsvReader.CsvRow row : CsvReader.read(directory.resolve("properties.csv"))) {
            store.addProperty(new Property(
                    row.get("property_id"),
                    row.get("neighborhood_id"),
                    row.get("property_type"),
                    integer(row, "bedrooms"),
                    integer(row, "bathrooms"),
                    number(row, "area_sqm"),
                    integer(row, "year_built"),
                    integer(row, "parking_spaces"),
                    integer(row, "floor"),
                    furnished(row.get("furnished")),
                    row.get("amenities"),
                    row.get("description")));
        }
        for (CsvReader.CsvRow row : CsvReader.read(directory.resolve("sales.csv"))) {
            store.addSale(new Sale(
                    row.get("sale_id"),
                    row.get("property_id"),
                    LocalDate.parse(row.get("sale_date")),
                    number(row, "sale_price")));
        }
        return store;
    }

    private static double number(CsvReader.CsvRow row, String column) {
        double value = Double.parseDouble(row.get(column));
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Non-finite numeric value in column: " + column);
        }
        return value;
    }

    private static int integer(CsvReader.CsvRow row, String column) {
        return Integer.parseInt(row.get(column));
    }

    private static boolean furnished(String raw) {
        return switch (raw.toLowerCase()) {
            case "1", "true", "yes" -> true;
            case "0", "false", "no" -> false;
            default -> throw new IllegalArgumentException("Invalid furnished flag: " + raw);
        };
    }
}
