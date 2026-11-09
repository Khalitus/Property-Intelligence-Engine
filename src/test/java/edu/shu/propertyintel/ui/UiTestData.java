package edu.shu.propertyintel.ui;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.time.LocalDate;

final class UiTestData {
    private UiTestData() {
    }

    static PropertyStore store() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N1", "Midtown", 2, 8, 8, 7));
        store.addNeighborhood(new Neighborhood("N2", "Riverside", 5, 7, 6, 8));
        store.addProperty(property("P0001", "N1", "House", 3));
        store.addProperty(property("P0002", "N2", "Apartment", 2));
        store.addProperty(property("P0003", "N1", "House", 1));
        store.addSale(new Sale("S1", "P0001", LocalDate.of(2024, 1, 1), 100_000));
        store.addSale(new Sale("S2", "P0001", LocalDate.of(2025, 1, 1), 120_000));
        store.addSale(new Sale("S3", "P0002", LocalDate.of(2025, 2, 1), 220_000));
        return store;
    }

    private static Property property(String id, String neighborhood, String type, int bedrooms) {
        return new Property(id, neighborhood, type, bedrooms, 2, 95, 2019,
                1, 2, false, "lift|parking", "Test data property");
    }
}
