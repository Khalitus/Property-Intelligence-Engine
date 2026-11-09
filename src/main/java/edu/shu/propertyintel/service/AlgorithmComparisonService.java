package edu.shu.propertyintel.service;

import edu.shu.propertyintel.algorithm.MergeSort;
import edu.shu.propertyintel.algorithm.QuickSort;
import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.dsa.CustomHashTable;
import edu.shu.propertyintel.model.Property;
import edu.shu.propertyintel.model.Sale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/** Small educational comparisons on the loaded dataset; not a scientific microbenchmark. */
public final class AlgorithmComparisonService {
    private static final int WARMUP_TRIALS = 2;
    private static final int MEASURED_TRIALS = 5;
    private static volatile long consumed;

    private final PropertyStore store;
    private final PropertySearchService search;

    public AlgorithmComparisonService(PropertyStore store, PropertySearchService search) {
        this.store = Objects.requireNonNull(store, "store");
        this.search = Objects.requireNonNull(search, "search");
    }

    public AlgorithmComparisonReport compare() {
        List<Property> all = new ArrayList<>();
        for (Property property : store.allProperties()) {
            all.add(property);
        }
        Property[] properties = all.toArray(new Property[0]);
        if (properties.length == 0) {
            return new AlgorithmComparisonReport(0, 0, null, null, List.of());
        }

        List<BenchmarkResult> results = new ArrayList<>();
        int lookupRepetitions = Math.max(1, Math.min(200, 4_000 / properties.length));
        int correctLookups = lookupAllHash(properties);
        if (correctLookups != lookupAllLinear(properties)) {
            throw new IllegalStateException("Hash and linear property lookups disagree");
        }
        results.add(new BenchmarkResult("Property ID lookup", "Hash table", "Linear scan", correctLookups,
                medianMicrosPerOperation(lookupRepetitions, properties.length,
                        () -> lookupAllHash(properties)),
                medianMicrosPerOperation(lookupRepetitions, properties.length,
                        () -> lookupAllLinear(properties))));

        double lowestPrice = Double.POSITIVE_INFINITY;
        double highestPrice = Double.NEGATIVE_INFINITY;
        int priced = 0;
        for (Property property : properties) {
            Sale latest = store.latestSaleFor(property.id());
            if (latest != null) {
                lowestPrice = Math.min(lowestPrice, latest.price());
                highestPrice = Math.max(highestPrice, latest.price());
                priced++;
            }
        }
        Double from = null;
        Double to = null;
        if (priced > 0) {
            double width = (highestPrice - lowestPrice) / 3;
            from = lowestPrice + width;
            to = highestPrice - width;
            double lower = from;
            double upper = to;
            List<Property> indexed = search.pricedPropertiesInRange(lower, upper);
            List<Property> linear = linearPriceRange(properties, lower, upper);
            assertSameProperties(indexed, linear);
            int rangeRepetitions = Math.max(5, Math.min(300, 3_000 / properties.length));
            results.add(new BenchmarkResult("Latest-sale price range", "AVL tree", "Linear scan", indexed.size(),
                    medianMicrosPerOperation(rangeRepetitions, 1,
                            () -> search.pricedPropertiesInRange(lower, upper).size()),
                    medianMicrosPerOperation(rangeRepetitions, 1,
                            () -> linearPriceRange(properties, lower, upper).size())));
        }

        Comparator<Property> byAreaThenId = Comparator.comparingDouble(Property::areaSqm)
                .thenComparing(Property::id);
        Property[] mergeCheck = properties.clone();
        Property[] quickCheck = properties.clone();
        MergeSort.sort(mergeCheck, byAreaThenId);
        QuickSort.sort(quickCheck, byAreaThenId);
        if (!Arrays.equals(mergeCheck, quickCheck)) {
            throw new IllegalStateException("Merge Sort and Quick Sort disagree");
        }
        int sortRepetitions = Math.max(5, Math.min(200, 3_000 / properties.length));
        results.add(new BenchmarkResult("Sort by area", "Merge Sort", "Quick Sort", properties.length,
                medianSortMicros(properties, sortRepetitions,
                        data -> MergeSort.sort(data, byAreaThenId)),
                medianSortMicros(properties, sortRepetitions,
                        data -> QuickSort.sort(data, byAreaThenId))));

        return new AlgorithmComparisonReport(properties.length, priced, from, to, results);
    }

    private int lookupAllHash(Property[] properties) {
        int matches = 0;
        for (Property query : properties) {
            if (store.findProperty(query.id()) != null) {
                matches++;
            }
        }
        return matches;
    }

    private static int lookupAllLinear(Property[] properties) {
        int matches = 0;
        for (Property query : properties) {
            for (Property candidate : properties) {
                if (candidate.id().equals(query.id())) {
                    matches++;
                    break;
                }
            }
        }
        return matches;
    }

    private List<Property> linearPriceRange(Property[] properties, double lower, double upper) {
        List<Property> matches = new ArrayList<>();
        for (Property property : properties) {
            Sale sale = store.latestSaleFor(property.id());
            if (sale != null && sale.price() >= lower && sale.price() <= upper) {
                matches.add(property);
            }
        }
        return matches;
    }


    private static void assertSameProperties(List<Property> first, List<Property> second) {
        if (first.size() != second.size()) {
            throw new IllegalStateException("Price range methods produced different result counts");
        }
        CustomHashTable<String, Boolean> ids = new CustomHashTable<>();
        for (Property property : first) {
            ids.put(property.id(), Boolean.TRUE);
        }
        if (ids.size() != first.size()) {
            throw new IllegalStateException("Duplicate property in AVL price range");
        }
        for (Property property : second) {
            if (!ids.containsKey(property.id())) {
                throw new IllegalStateException("Price range methods produced different properties");
            }
        }
    }

    private static double medianMicrosPerOperation(int repetitions, int operationsPerRepetition,
                                                    IntSupplier operation) {
        double[] measurements = new double[MEASURED_TRIALS];
        for (int trial = -WARMUP_TRIALS; trial < MEASURED_TRIALS; trial++) {
            long checksum = 0;
            long start = System.nanoTime();
            for (int repeat = 0; repeat < repetitions; repeat++) {
                checksum += operation.getAsInt();
            }
            long duration = System.nanoTime() - start;
            consumed = checksum;
            if (trial >= 0) {
                measurements[trial] = duration / (1_000.0 * repetitions * operationsPerRepetition);
            }
        }
        return median(measurements);
    }

    private static double medianSortMicros(Property[] source, int repetitions, Consumer<Property[]> sorter) {
        double[] measurements = new double[MEASURED_TRIALS];
        for (int trial = -WARMUP_TRIALS; trial < MEASURED_TRIALS; trial++) {
            long nanoseconds = 0;
            long checksum = 0;
            for (int repeat = 0; repeat < repetitions; repeat++) {
                Property[] copy = source.clone();
                long start = System.nanoTime();
                sorter.accept(copy);
                nanoseconds += System.nanoTime() - start;
                checksum += copy.length == 0 ? 0 : copy[0].id().length();
            }
            consumed = checksum;
            if (trial >= 0) {
                measurements[trial] = nanoseconds / (1_000.0 * repetitions);
            }
        }
        return median(measurements);
    }

    private static double median(double[] values) {
        double[] ordered = values.clone();
        for (int i = 1; i < ordered.length; i++) {
            double current = ordered[i];
            int j = i - 1;
            while (j >= 0 && ordered[j] > current) {
                ordered[j + 1] = ordered[j];
                j--;
            }
            ordered[j + 1] = current;
        }
        return ordered[ordered.length / 2];
    }
}
