package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertySearchServiceTest {
    private static Property property(String id, String area, String neighborhoodId, String type, int beds) {
        return new Property(id, neighborhoodId, type, beds, 2, Double.parseDouble(area),
                2015, 1, 0, false, "parking", "Sample");
    }

    private static PropertyStore store() {
        PropertyStore store = new PropertyStore();
        store.addNeighborhood(new Neighborhood("N01", "Midtown", 2, 8, 8, 8));
        store.addNeighborhood(new Neighborhood("N02", "Old Town", 3, 6, 7, 6));
        store.addProperty(property("P01", "150", "N01", "House", 3));
        store.addProperty(property("P02", "200", "N01", "House", 4));
        store.addProperty(property("P03", "180", "N02", "Apartment", 2));
        store.addProperty(property("P04", "175", "N01", "House", 3));
        store.addProperty(property("P05", "120", "N02", "Apartment", 1));
        store.addSale(new Sale("S1", "P01", LocalDate.of(2024, 1, 1), 250));
        store.addSale(new Sale("S2", "P01", LocalDate.of(2026, 1, 1), 400));
        store.addSale(new Sale("S3", "P02", LocalDate.of(2025, 1, 1), 600));
        store.addSale(new Sale("S4", "P03", LocalDate.of(2025, 1, 1), 400));
        store.addSale(new Sale("S5", "P04", LocalDate.of(2025, 1, 1), 700));
        return store;
    }

    @Test
    void combinesPriceTypeNeighborhoodAndBedroomFilters() {
        PropertySearchService service = new PropertySearchService(store());
        assertEquals(4, service.indexedPropertyCount());
        SearchCriteria criteria = new SearchCriteria("house", "midtown", 3, 400.0, 650.0);
        assertEquals(List.of("P01", "P02"), ids(service.search(criteria, SortField.PRICE, SortAlgorithm.MERGE, true)));
        assertEquals(List.of("P02", "P01"), ids(service.search(criteria, SortField.PRICE, SortAlgorithm.QUICK, false)));
        assertEquals(List.of("P01", "P02"), ids(service.search(
                new SearchCriteria("House", "n01", 3, 400.0, 650.0),
                SortField.PRICE, SortAlgorithm.QUICK, true)));
    }

    @Test
    void includesEqualPricePropertiesAndUsesMostRecentSale() {
        PropertySearchService service = new PropertySearchService(store());
        List<Property> matches = service.search(new SearchCriteria(null, null, null, 400.0, 400.0),
                SortField.AREA, SortAlgorithm.MERGE, true);
        assertEquals(List.of("P01", "P03"), ids(matches));
    }

    @Test
    void excludesUnsoldPropertiesFromPriceFilterButKeepsThemOtherwise() {
        PropertySearchService service = new PropertySearchService(store());
        assertEquals(List.of("P01", "P03", "P02", "P04", "P05"), ids(service.search(
                new SearchCriteria(null, null, null, null, null), SortField.PRICE, SortAlgorithm.MERGE, true)));
        assertEquals(List.of("P04", "P02", "P01", "P03"), ids(service.search(
                new SearchCriteria(null, null, null, 300.0, null), SortField.PRICE, SortAlgorithm.QUICK, false)));
    }

    @Test
    void sortsByAreaBedroomsAndYear() {
        PropertySearchService service = new PropertySearchService(store());
        SearchCriteria any = new SearchCriteria(null, null, null, null, null);
        assertEquals(List.of("P05", "P01", "P04", "P03", "P02"),
                ids(service.search(any, SortField.AREA, SortAlgorithm.QUICK, true)));
        assertEquals(List.of("P02", "P01", "P04", "P03", "P05"),
                ids(service.search(any, SortField.BEDROOMS, SortAlgorithm.MERGE, false)));
        assertEquals(List.of("P01", "P02", "P03", "P04", "P05"),
                ids(service.search(any, SortField.YEAR_BUILT, SortAlgorithm.QUICK, true)));
    }

    @Test
    void rejectsInvalidFiltersAndReturnsEmptyMatches() {
        PropertySearchService service = new PropertySearchService(store());
        assertThrows(IllegalArgumentException.class,
                () -> new SearchCriteria(null, null, null, 700.0, 300.0));
        assertThrows(IllegalArgumentException.class,
                () -> new SearchCriteria(null, null, -1, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new SearchCriteria(null, null, null, Double.NaN, null));
        assertEquals(0, service.search(new SearchCriteria("Villa", null, null, null, null),
                SortField.AREA, SortAlgorithm.MERGE, true).size());
    }

    private static List<String> ids(List<Property> properties) {
        return properties.stream().map(Property::id).toList();
    }
}
