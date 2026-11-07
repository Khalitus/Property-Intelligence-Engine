package edu.shu.propertyintel.model;

public record Neighborhood(
        String id,
        String name,
        double distanceToCenterKm,
        double schoolScore,
        double transitScore,
        double safetyScore
) {
}
