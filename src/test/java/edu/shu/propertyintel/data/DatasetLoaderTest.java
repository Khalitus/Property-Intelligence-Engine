package edu.shu.propertyintel.data;

import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DatasetLoaderTest {
    @TempDir
    Path tempDir;

    private void writeDataset(String propertyNeighborhood) throws IOException {
        Files.writeString(tempDir.resolve("neighborhoods.csv"),
                "neighborhood_id,neighborhood_name,distance_to_center_km,school_score,transit_score,safety_score\n"
                        + "N01,Midtown,2.7,9,8,7\n");
        Files.writeString(tempDir.resolve("properties.csv"),
                "property_id,neighborhood_id,property_type,bedrooms,bathrooms,area_sqm,year_built,parking_spaces,floor,furnished,amenities,description\n"
                        + "P0001," + propertyNeighborhood + ",House,3,2,175,2010,1,1,1,parking,\"Near schools, shops\"\n");
        Files.writeString(tempDir.resolve("sales.csv"),
                "sale_id,property_id,sale_date,sale_price\n"
                        + "S02,P0001,2026-04-01,525000\n"
                        + "S01,P0001,2024-04-01,450000\n");
    }

    @Test
    void loadsAndLinksAllThreeCsvFiles() throws IOException {
        writeDataset("N01");
        PropertyStore store = new DatasetLoader().load(tempDir);
        assertEquals(1, store.propertyCount());
        assertEquals(1, store.neighborhoodCount());
        assertEquals(2, store.saleCount());
        Property property = store.findProperty("P0001");
        assertEquals("Near schools, shops", property.description());
        assertTrue(property.furnished());
        assertEquals("Midtown", store.findNeighborhood(property.neighborhoodId()).name());
        assertEquals("S01", store.salesFor("P0001").getFirst().id());
        Sale latest = store.latestSaleFor("P0001");
        assertEquals("S02", latest.id());
        assertEquals(525000, latest.price());
        assertNull(store.findProperty("NOT_FOUND"));
    }

    @Test
    void rejectsUnknownNeighborhoodReferences() throws IOException {
        writeDataset("INVALID");
        assertThrows(IllegalArgumentException.class, () -> new DatasetLoader().load(tempDir));
    }
}
