package edu.shu.propertyintel.data;

import edu.shu.propertyintel.dsa.CustomHashTable;
import edu.shu.propertyintel.dsa.CustomLinkedList;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.util.Comparator;

public final class PropertyStore {
    private static final Comparator<Sale> CHRONOLOGICAL =
            Comparator.comparing(Sale::date).thenComparing(Sale::id);

    private final CustomHashTable<String, Property> propertiesById = new CustomHashTable<>();
    private final CustomHashTable<String, Neighborhood> neighborhoodsById = new CustomHashTable<>();
    private final CustomHashTable<String, CustomLinkedList<Sale>> salesByProperty = new CustomHashTable<>();
    private final CustomHashTable<String, Boolean> saleIds = new CustomHashTable<>();
    private final CustomLinkedList<Property> properties = new CustomLinkedList<>();
    private int saleCount;

    public void addNeighborhood(Neighborhood neighborhood) {
        if (neighborhoodsById.containsKey(neighborhood.id())) {
            throw new IllegalArgumentException("Duplicate neighborhood ID: " + neighborhood.id());
        }
        neighborhoodsById.put(neighborhood.id(), neighborhood);
    }

    public void addProperty(Property property) {
        if (!neighborhoodsById.containsKey(property.neighborhoodId())) {
            throw new IllegalArgumentException("Unknown neighborhood: " + property.neighborhoodId());
        }
        if (propertiesById.containsKey(property.id())) {
            throw new IllegalArgumentException("Duplicate property ID: " + property.id());
        }
        propertiesById.put(property.id(), property);
        properties.addLast(property);
    }

    public void addSale(Sale sale) {
        if (!propertiesById.containsKey(sale.propertyId())) {
            throw new IllegalArgumentException("Unknown property for sale: " + sale.propertyId());
        }
        if (saleIds.containsKey(sale.id())) {
            throw new IllegalArgumentException("Duplicate sale ID: " + sale.id());
        }
        saleIds.put(sale.id(), Boolean.TRUE);
        CustomLinkedList<Sale> history = salesByProperty.get(sale.propertyId());
        if (history == null) {
            history = new CustomLinkedList<>();
            salesByProperty.put(sale.propertyId(), history);
        }
        history.insertSorted(sale, CHRONOLOGICAL);
        saleCount++;
    }

    public Property findProperty(String id) {
        return propertiesById.get(id);
    }

    public Neighborhood findNeighborhood(String id) {
        return neighborhoodsById.get(id);
    }

    public CustomLinkedList<Sale> salesFor(String propertyId) {
        CustomLinkedList<Sale> history = salesByProperty.get(propertyId);
        return history == null ? new CustomLinkedList<>() : history;
    }

    public Sale latestSaleFor(String propertyId) {
        CustomLinkedList<Sale> history = salesByProperty.get(propertyId);
        return history == null || history.isEmpty() ? null : history.getLast();
    }

    public Iterable<Property> allProperties() {
        return properties;
    }

    public int propertyCount() {
        return propertiesById.size();
    }

    public int neighborhoodCount() {
        return neighborhoodsById.size();
    }

    public int saleCount() {
        return saleCount;
    }
}
