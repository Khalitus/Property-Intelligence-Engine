package edu.shu.propertyintel.service;

import edu.shu.propertyintel.model.Property;

public record ComparableProperty(Property property, double latestPrice, double similarity) {
}
