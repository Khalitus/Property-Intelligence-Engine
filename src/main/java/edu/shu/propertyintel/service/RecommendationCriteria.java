package edu.shu.propertyintel.service;

public record RecommendationCriteria(
        double maximumBudget,
        int minimumBedrooms,
        double minimumAreaSqm,
        String propertyType,
        String neighborhood
) {
    public RecommendationCriteria {
        if (!Double.isFinite(maximumBudget) || maximumBudget <= 0) {
            throw new IllegalArgumentException("Maximum budget must be a positive number");
        }
        if (minimumBedrooms < 0) {
            throw new IllegalArgumentException("Minimum bedrooms cannot be negative");
        }
        if (!Double.isFinite(minimumAreaSqm) || minimumAreaSqm < 0) {
            throw new IllegalArgumentException("Minimum area cannot be negative");
        }
        propertyType = propertyType == null ? "" : propertyType.trim();
        neighborhood = neighborhood == null ? "" : neighborhood.trim();
    }
}
