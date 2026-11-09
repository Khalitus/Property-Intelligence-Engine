package edu.shu.propertyintel.service;

import edu.shu.propertyintel.algorithm.MergeSort;
import edu.shu.propertyintel.algorithm.QuickSort;
import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.dsa.AVLTree;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class PropertySearchService {
    private final PropertyStore store;
    private final AVLTree<Double, Property> priceIndex = new AVLTree<>();

    public PropertySearchService(PropertyStore store) {
        this.store = Objects.requireNonNull(store, "store");
        for (Property property : store.allProperties()) {
            Sale latest = store.latestSaleFor(property.id());
            if (latest != null) {
                priceIndex.insert(latest.price(), property);
            }
        }
    }

    public int indexedPropertyCount() {
        return priceIndex.size();
    }

    public int priceIndexHeight() {
        return priceIndex.height();
    }

    List<Property> pricedPropertiesInRange(double minimum, double maximum) {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum < 0 || minimum > maximum) {
            throw new IllegalArgumentException("Invalid price range");
        }
        return priceIndex.rangeSearch(minimum, maximum);
    }

    public List<Property> search(SearchCriteria criteria, SortField sortField,
                                 SortAlgorithm algorithm, boolean ascending) {
        Objects.requireNonNull(criteria, "criteria");
        Objects.requireNonNull(sortField, "sortField");
        Objects.requireNonNull(algorithm, "algorithm");

        Iterable<Property> candidates;
        if (criteria.minimumPrice() != null || criteria.maximumPrice() != null) {
            double minimum = criteria.minimumPrice() == null ? Double.NEGATIVE_INFINITY : criteria.minimumPrice();
            double maximum = criteria.maximumPrice() == null ? Double.POSITIVE_INFINITY : criteria.maximumPrice();
            candidates = priceIndex.rangeSearch(minimum, maximum);
        } else {
            candidates = store.allProperties();
        }

        List<Property> matches = new ArrayList<>();
        for (Property property : candidates) {
            if (matches(property, criteria)) {
                matches.add(property);
            }
        }

        Property[] sorted = matches.toArray(new Property[0]);
        Comparator<Property> comparator = comparator(sortField, ascending);
        if (algorithm == SortAlgorithm.MERGE) {
            MergeSort.sort(sorted, comparator);
        } else {
            QuickSort.sort(sorted, comparator);
        }
        return List.of(sorted);
    }

    private boolean matches(Property property, SearchCriteria criteria) {
        if (hasText(criteria.type()) && !property.type().equalsIgnoreCase(criteria.type().trim())) {
            return false;
        }
        if (criteria.minimumBedrooms() != null && property.bedrooms() < criteria.minimumBedrooms()) {
            return false;
        }
        if (hasText(criteria.neighborhood())) {
            Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
            String query = criteria.neighborhood().trim();
            if (!property.neighborhoodId().equalsIgnoreCase(query)
                    && !neighborhood.name().equalsIgnoreCase(query)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Comparator<Property> comparator(SortField sortField, boolean ascending) {
        return (first, second) -> {
            if (sortField == SortField.PRICE) {
                Sale firstSale = store.latestSaleFor(first.id());
                Sale secondSale = store.latestSaleFor(second.id());
                if (firstSale == null && secondSale != null) {
                    return 1;
                }
                if (firstSale != null && secondSale == null) {
                    return -1;
                }
                if (firstSale != null) {
                    int difference = Double.compare(firstSale.price(), secondSale.price());
                    if (difference != 0) {
                        return ascending ? difference : -difference;
                    }
                }
            } else {
                int difference = switch (sortField) {
                    case AREA -> Double.compare(first.areaSqm(), second.areaSqm());
                    case BEDROOMS -> Integer.compare(first.bedrooms(), second.bedrooms());
                    case YEAR_BUILT -> Integer.compare(first.yearBuilt(), second.yearBuilt());
                    case PRICE -> 0;
                };
                if (difference != 0) {
                    return ascending ? difference : -difference;
                }
            }
            return first.id().toUpperCase(Locale.ROOT)
                    .compareTo(second.id().toUpperCase(Locale.ROOT));
        };
    }
}
