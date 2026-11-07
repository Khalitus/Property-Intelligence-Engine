package edu.shu.propertyintel.model;

public record Property(
        String id,
        String neighborhoodId,
        String type,
        int bedrooms,
        int bathrooms,
        double areaSqm,
        int yearBuilt,
        int parkingSpaces,
        int floor,
        boolean furnished,
        String amenities,
        String description
) {
}
