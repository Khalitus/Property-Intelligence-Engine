package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.dsa.MaxHeap;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Estimates a property's value from the latest sales of the most similar same-type properties. */
public final class ComparableValuationService {
    private static final Comparator<ComparableProperty> MOST_SIMILAR =
            Comparator.comparingDouble(ComparableProperty::similarity)
                    .thenComparing(result -> result.property().id(), Comparator.reverseOrder());

    private final PropertyStore store;

    public ComparableValuationService(PropertyStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public Optional<ValuationResult> estimate(String propertyId, int comparableCount) {
        if (propertyId == null || propertyId.isBlank()) {
            throw new IllegalArgumentException("Property ID is required");
        }
        if (comparableCount < 1) {
            throw new IllegalArgumentException("Comparable count must be positive");
        }
        Property subject = store.findProperty(propertyId.trim().toUpperCase(java.util.Locale.ROOT));
        if (subject == null) {
            throw new IllegalArgumentException("Property not found: " + propertyId);
        }
        MaxHeap<ComparableProperty> heap = new MaxHeap<>(MOST_SIMILAR);
        for (Property candidate : store.allProperties()) {
            if (candidate.id().equals(subject.id()) || !candidate.type().equalsIgnoreCase(subject.type())) {
                continue;
            }
            Sale latest = store.latestSaleFor(candidate.id());
            if (latest == null || !Double.isFinite(latest.price()) || latest.price() <= 0) {
                continue;
            }
            double distance = difference(subject, candidate);
            heap.add(new ComparableProperty(candidate, latest.price(), 1.0 / (1.0 + distance)));
        }
        if (heap.isEmpty()) {
            return Optional.empty();
        }
        List<ComparableProperty> selected = new ArrayList<>();
        double total = 0;
        while (!heap.isEmpty() && selected.size() < comparableCount) {
            ComparableProperty comparable = heap.poll();
            selected.add(comparable);
            total += comparable.latestPrice();
        }
        return Optional.of(new ValuationResult(subject, total / selected.size(), selected));
    }

    private static double difference(Property a, Property b) {
        double bedroomPenalty = Math.abs(a.bedrooms() - b.bedrooms()) * 4.0;
        double areaPenalty = Math.abs(a.areaSqm() - b.areaSqm()) / Math.max(a.areaSqm(), 1.0) * 6.0;
        double bathroomPenalty = Math.abs(a.bathrooms() - b.bathrooms()) * 0.75;
        double parkingPenalty = Math.abs(a.parkingSpaces() - b.parkingSpaces()) * 0.5;
        double neighborhoodPenalty = a.neighborhoodId().equalsIgnoreCase(b.neighborhoodId()) ? 0 : 1.5;
        return bedroomPenalty + areaPenalty + bathroomPenalty + parkingPenalty + neighborhoodPenalty;
    }
}
