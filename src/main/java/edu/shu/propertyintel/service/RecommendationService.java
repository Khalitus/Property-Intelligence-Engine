package edu.shu.propertyintel.service;

import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.dsa.MaxHeap;
import edu.shu.propertyintel.model.Neighborhood;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class RecommendationService {
    private static final Comparator<PropertyRecommendation> BEST_FIRST =
            Comparator.comparingDouble(PropertyRecommendation::score)
                    .thenComparing(result -> result.property().id(), Comparator.reverseOrder());

    private final PropertyStore store;

    public RecommendationService(PropertyStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public List<PropertyRecommendation> recommend(RecommendationCriteria criteria, int limit) {
        Objects.requireNonNull(criteria, "criteria");
        if (limit < 1) {
            throw new IllegalArgumentException("Result limit must be positive");
        }
        MaxHeap<PropertyRecommendation> heap = new MaxHeap<>(BEST_FIRST);
        for (Property property : store.allProperties()) {
            Sale latest = store.latestSaleFor(property.id());
            if (latest == null || !Double.isFinite(latest.price()) || latest.price() < 0
                    || latest.price() > criteria.maximumBudget()
                    || property.bedrooms() < criteria.minimumBedrooms()
                    || property.areaSqm() < criteria.minimumAreaSqm()
                    || !criteria.propertyType().isEmpty()
                        && !property.type().equalsIgnoreCase(criteria.propertyType())) {
                continue;
            }
            Neighborhood neighborhood = store.findNeighborhood(property.neighborhoodId());
            if (!criteria.neighborhood().isEmpty()
                    && !property.neighborhoodId().equalsIgnoreCase(criteria.neighborhood())
                    && !neighborhood.name().equalsIgnoreCase(criteria.neighborhood())) {
                continue;
            }
            heap.add(new PropertyRecommendation(property, latest.price(), score(criteria, property, latest, neighborhood)));
        }
        List<PropertyRecommendation> results = new ArrayList<>();
        while (!heap.isEmpty() && results.size() < limit) {
            results.add(heap.poll());
        }
        return List.copyOf(results);
    }

    /** A transparent 0–100 preference score, not a probability or ML prediction. */
    private double score(RecommendationCriteria criteria, Property property, Sale sale,
                         Neighborhood neighborhood) {
        double affordability = 25 * (criteria.maximumBudget() - sale.price()) / criteria.maximumBudget();
        double school = 20 * clamp(neighborhood.schoolScore(), 0, 10) / 10;
        double safety = 20 * clamp(neighborhood.safetyScore(), 0, 10) / 10;
        double transit = 10 * clamp(neighborhood.transitScore(), 0, 10) / 10;
        double targetArea = Math.max(150, criteria.minimumAreaSqm() * 1.5);
        double area = 15 * clamp(property.areaSqm() / targetArea, 0, 1);
        double parking = 10 * clamp(property.parkingSpaces() / 2.0, 0, 1);
        return affordability + school + safety + transit + area + parking;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
