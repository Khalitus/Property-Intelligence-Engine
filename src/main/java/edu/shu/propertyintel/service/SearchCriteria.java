package edu.shu.propertyintel.service;

public record SearchCriteria(
        String type,
        String neighborhood,
        Integer minimumBedrooms,
        Double minimumPrice,
        Double maximumPrice
) {
    public SearchCriteria {
        if (minimumBedrooms != null && minimumBedrooms < 0) {
            throw new IllegalArgumentException("Minimum bedrooms must be zero or greater");
        }
        if (minimumPrice != null && (!Double.isFinite(minimumPrice) || minimumPrice < 0)) {
            throw new IllegalArgumentException("Minimum price must be a finite nonnegative number");
        }
        if (maximumPrice != null && (!Double.isFinite(maximumPrice) || maximumPrice < 0)) {
            throw new IllegalArgumentException("Maximum price must be a finite nonnegative number");
        }
        if (minimumPrice != null && maximumPrice != null && minimumPrice > maximumPrice) {
            throw new IllegalArgumentException("Minimum price must not exceed maximum price");
        }
    }
}
