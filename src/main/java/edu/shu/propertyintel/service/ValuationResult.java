package edu.shu.propertyintel.service;

import edu.shu.propertyintel.model.Property;
import java.util.List;

public record ValuationResult(Property property, double estimatedPrice,
                              List<ComparableProperty> comparables) {
    public ValuationResult {
        comparables = List.copyOf(comparables);
    }
}
