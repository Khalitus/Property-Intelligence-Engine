package edu.shu.propertyintel.service;

public record BenchmarkResult(
        String operation,
        String firstApproach,
        String secondApproach,
        int resultCount,
        double firstMicroseconds,
        double secondMicroseconds
) {
}
