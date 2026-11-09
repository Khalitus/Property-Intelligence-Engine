# Property Intelligence Engine — DSA Edition

A Java 21 application for exploring property records, historical sales, recommendations and comparable-sale valuations. **Task 4 completes the command-line application.** Tasks 5–6 will add a Swing desktop interface using the same services.

The central data structures and algorithms are implemented in this repository rather than delegated to `HashMap`, `TreeMap`, `PriorityQueue`, `Arrays.sort` or `Collections.sort`.

## Requirements

- Java Development Kit 21
- Apache Maven 3.9 or later
- A terminal opened at the repository root (where `pom.xml` is located)

## Run

```bash
mvn clean test
mvn exec:java
```

If macOS AppleDouble `._*` files on an external SSD interfere with JUnit test discovery, run the build and test phases separately:

```bash
mvn clean test-compile
find target -type f -name '._*' -delete
mvn surefire:test
```

To execute compiled classes directly without Maven:

```bash
java -cp target/classes edu.shu.propertyintel.Main data
```

The optional argument is the directory holding the three CSV files. With `mvn exec:java`, the default dataset directory is `data/`.

## CLI features

```text
PROPERTY INTELLIGENCE ENGINE
1. Dataset summary
2. Browse properties
3. Find property by ID
4. View sale history
5. Search, filter and sort properties
6. Recommend properties
7. Estimate a property value
8. Compare algorithms
0. Exit
```

1. **Dashboard summary:** property, sale and neighborhood counts, number of properties with an indexed latest sale, AVL height.
2. **Browse:** print a specified number of property records.
3. **ID lookup:** retrieve a property using the custom hash table.
4. **Sale history:** print chronologically ordered sales and absolute/percentage changes.
5. **Search:** filter by property type, neighborhood name/ID, minimum bedrooms, and inclusive latest-sale-price bounds; sort by price, area, bedrooms or year built using Merge Sort or Quick Sort, in either direction.
6. **Recommendations:** find up to five affordable properties meeting required bedrooms, area, type and neighborhood; rank eligible properties with a custom Max Heap and a transparent preference score.
7. **Comparable valuation:** find up to three same-type properties with recorded sales, rank by similarity, and average their latest sale prices. This is a synthetic-data heuristic, **not** a certified appraisal or ML prediction.
8. **Algorithm comparison:** compare property lookup (hash table vs linear scan), price-range retrieval (AVL vs linear scan), and area sorting (Merge Sort vs Quick Sort) on the loaded dataset.

Blank search filters are optional. Default values are displayed in square brackets. Invalid input is reported without terminating the application.

## Dataset

The included `data/` directory contains **12 illustrative properties, 24 sales, and 6 neighborhoods**. It is a development fixture, not a real housing market dataset.

To use the original larger synthetic dataset, replace the three files with the matching schema from [property-intelligence-engine/data](https://github.com/Khalitus/applied-agentic-ai-projects/tree/main/property-intelligence-engine/data):

```text
data/properties.csv
data/sales.csv
data/neighborhoods.csv
```

The CSV parser supports quoted fields, escaped quotes, embedded newlines and UTF-8 input. The loader verifies references between neighborhoods, properties and sales and rejects invalid numeric values. The application is currently read-only: the CSV data is loaded once at startup and indexes are built from those records.

## Architecture

```text
CSV -> DatasetLoader -> PropertyStore
                           |  |  |
                           |  |  +-- custom linked lists (sale history)
                           |  +----- custom hash tables (IDs)
                           |
             +-------------+---------------------+
             |             |                     |
   PropertySearchService  RecommendationService  ComparableValuationService
   AVL index + filters    preference scoring     similarity ranking
   Merge Sort / Quick     MaxHeap                 MaxHeap
             |             |                     |
             +-------------+---------------------+
                           |
                  ConsoleApplication
                           |
                AlgorithmComparisonService
```

The CLI is an input/output layer. Business logic remains in reusable services so the upcoming Swing GUI can call the same methods without reimplementing search, ranking or valuation.

### Main packages

| Package | Responsibility |
|---|---|
| `model` | Immutable `Property`, `Sale`, `Neighborhood` records |
| `data` | CSV parsing, loading, linked histories and ID-keyed property repository |
| `dsa` | Custom linked list, hash table, AVL tree and max heap |
| `algorithm` | Custom generic Merge Sort and Quick Sort |
| `service` | Search, recommendation, valuation and algorithm comparison |
| `cli` | Validated interactive command-line menus and formatted output |

## DSA complexity

Let `n` be the number of properties, `u` distinct latest-sale prices, `k` matches returned by a price range, and `r` final search matches.

| Operation | Expected time complexity | Notes |
|---|---|---|
| Linked list append | O(1) | Maintains a tail pointer |
| Sorted sale-history insertion | O(h) | `h` = sales for the property |
| Hash table lookup | O(1) average, O(n) worst | Separate chaining; automatic resizing |
| AVL price lookup | O(log u) | Balanced tree; duplicate prices share a node |
| AVL price range retrieval | O(log u + k) | Prunes irrelevant subtrees |
| Search without price bounds | O(n) before sorting | Additional filters are evaluated sequentially |
| Merge Sort | O(r log r) | Stable; O(r) extra memory |
| Quick Sort | O(r log r) average, O(r²) worst | Three-way partition, median-of-three pivot |
| Heap insertion / extraction | O(log n) | Uses an array-backed binary max heap |
| Top-five recommendations | O(n log n) + O(5 log n) | Filter and score, heap insertion and extraction |
| Comparable valuation | O(n log n) + O(c log n) | `c` = selected comparables; uses same heap |

These complexities describe the algorithms, not guaranteed wall-clock speed. Price bounds reference the **latest dated sale** for each property. Properties with no recorded sale can be browsed and filtered without price bounds, but cannot satisfy a price range or enter price-based recommendations.

## Task 4: measured algorithm comparisons

Option 8 compares implementations using `System.nanoTime()` on the current dataset:

- **ID lookups:** every property ID is queried using `PropertyStore.findProperty()` and a sequential array scan. Both perform the same set of successful lookups.
- **Price ranges:** an inclusive range spanning the middle third of the dataset's observed latest-sale prices is queried through the existing AVL index and a sequential scan. Both result sets are checked for equivalence before measurement. This comparison is skipped when no sales exist.
- **Sorting:** each method receives a fresh copy of the same unsorted property array; both use the same area-then-ID comparator. Their output order is checked before measurement.

For each approach, two warm-up trials are followed by five measured trials; the reported figure is the median time in **microseconds per operation**. Input copying is excluded from the sort measurement. Results are computed on the user's machine, not hard-coded. They are educational measurements, **not rigorous JMH benchmarks**: short executions, JVM optimization, operating system scheduling, hardware and data distribution influence the timings. No algorithm is guaranteed to appear faster on a small sample.

## Testing and validation

The test suite covers data parsing, reference checking, numeric validation, hashing and collisions, linked-list ordering, AVL rotations and duplicate keys, sorting correctness, heap ordering, search filters, recommendation scoring, comparable valuation, algorithm comparison, and all eight CLI options in one scripted session.

Run `mvn clean test`; Maven Surefire prints the exact number of executed JUnit tests. Do not treat a compilation-only run or `-DskipTests` as a completed validation. The CLI can also be exercised manually using `mvn exec:java`.

## Current limitations

- Data is synthetic and read-only; no live market feed or persistence of edits.
- Recommendations use a fixed scoring formula rather than learned personal preferences.
- Comparable valuation averages a small set of related past sales; it does not estimate uncertainty or market dynamics.
- Benchmark readings are illustrative micro-timings. Production performance investigation would use larger controlled datasets and JMH.
- The graphical interface is scheduled for Tasks 5–6.

## Development roadmap

- [x] Task 1 — Data models, CSV reader, linked list, hash table, base CLI
- [x] Task 2 — AVL price index, property filters, Merge Sort, Quick Sort
- [x] Task 3 — Max Heap, recommendations, comparable valuation
- [x] Task 4 — Complete CLI, validation, integration tests, algorithm comparison
- [ ] Task 5 — Swing dashboard, search and sale history
- [ ] Task 6 — Swing recommendations, valuation and integration
- [ ] Task 7 — Final technical documentation, screenshots and report
