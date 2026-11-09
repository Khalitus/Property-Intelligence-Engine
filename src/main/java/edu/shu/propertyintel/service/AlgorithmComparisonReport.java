package edu.shu.propertyintel.service;

import java.util.List;

public record AlgorithmComparisonReport(
        int propertyCount,
        int pricedPropertyCount,
        Double minimumRangePrice,
        Double maximumRangePrice,
        List<BenchmarkResult> results
) {
    public AlgorithmComparisonReport {
        results = List.copyOf(results);
    }
}
