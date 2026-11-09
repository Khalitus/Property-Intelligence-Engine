# Property Intelligence Engine — DSA Edition

A Java 21 application for exploring property records, historical sales, recommendations and comparable-sale valuations. **Tasks 1–4 complete the CLI; Tasks 5–6 complete the Swing desktop application.** Both interfaces use the same DSA-backed services. No database, network or machine-learning dependency is required.

The central data structures and algorithms are implemented in this repository rather than delegated to `HashMap`, `TreeMap`, `PriorityQueue`, `Arrays.sort` or `Collections.sort`.

## Requirements

- Java Development Kit 21
- Apache Maven 3.9 or later
- A terminal opened at the repository root (where `pom.xml` is located)

## Run

```bash
mvn clean test
mvn exec:java                  # original command-line application
mvn -Pdesktop exec:java        # Swing desktop application
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

## Desktop interface — Tasks 5 and 6

```bash
mvn clean test
mvn -Pdesktop exec:java
```

Alternatively, after compiling: `java -cp target/classes edu.shu.propertyintel.DesktopMain`. The normal `mvn exec:java` still opens the CLI. Both interfaces load the three CSV files from `data/` by default; the direct Java entry points accept an optional alternative data-directory argument.

### Screens

| Screen | What the user can do |
|---|---|
| Dashboard | View dataset counts, indexed-property statistics and latest-sale price range; open property browsing |
| Properties | Filter by type, neighborhood, minimum bedrooms and price range; choose Merge/Quick Sort and direction; open details or sale history |
| History | View chronological transactions and calculate changes between recorded sales |
| Recommendations | Enter a maximum budget and optional minimum bedrooms, area, type and neighborhood; receive up to five heap-ranked properties with scoring explanation |
| Valuation | Enter a property ID and choose 1–5 comparable sales; view the estimated average and the underlying same-type comparable properties |
| Algorithms | Run and view measured Hash Table vs linear scan, AVL vs linear scan, and Merge Sort vs Quick Sort comparisons |

### Usability and validation

- Primary actions have dark blue backgrounds with **white** text; secondary buttons and unselected navigation have light backgrounds with **dark** text. The button drawing and foreground color are explicitly controlled, avoiding macOS Look-and-Feel overrides.
- Known property types and neighborhood names are selected from dropdowns. Price fields accept up to 12 digits and two decimal places; bedrooms are restricted to 0–20; recommendation area is 0–100,000 m²; valuation compares 1–5 properties.
- Empty and invalid inputs display a next-step explanation. An invalid calculation does not overwrite the previous successful results; a valid search with zero matches explicitly clears old results.
- Tables are read-only. Results are sorted by the project's own algorithms rather than Swing's built-in row sorter. Narrower windows scroll tables horizontally and long explanations vertically.
- Recommendation scores are preferences on a 0–100 scale, **not confidence probabilities**. Valuation's relative match index is also not a confidence probability. All prices are illustrative synthetic dataset units, not current listings or certified appraisals.
- The algorithm comparison uses `SwingWorker` so the Event Dispatch Thread remains responsive. It disables Run while work is underway and reports errors or results when finished.
- The GUI loads the CSV dataset once at startup and reuses `PropertyStore`, `PropertySearchService`, `RecommendationService`, `ComparableValuationService` and `AlgorithmComparisonService`. It does not duplicate their DSA logic.

### GUI architecture

`DesktopMain` opens `MainWindow` on Swing's Event Dispatch Thread. `MainWindow` maintains six page panels with `CardLayout`. The `ui` package contains the view panels (`DashboardPanel`, `SearchPanel`, `HistoryPanel`, `RecommendationPanel`, `ValuationPanel`, `ComparisonPanel`), the `JTable` adapters, `InputRules`, and the shared `UiTheme`. Backend implementation lives in `data`, `dsa`, `algorithm` and `service` packages.

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
                ConsoleApplication      Swing UI (Tasks 5–6)
                           |                  |
                AlgorithmComparisonService    Six navigation pages
```

The CLI and Swing UI are separate presentation layers. They reuse the same services without reimplementing search, ranking, sorting or valuation.

### Main packages

| Package | Responsibility |
|---|---|
| `model` | Immutable `Property`, `Sale`, `Neighborhood` records |
| `data` | CSV parsing, loading, linked histories and ID-keyed property repository |
| `dsa` | Custom linked list, hash table, AVL tree and max heap |
| `algorithm` | Custom generic Merge Sort and Quick Sort |
| `service` | Search, recommendation, valuation and algorithm comparison |
| `cli` | Validated interactive command-line menus and formatted output |
| `ui` | Swing view components, table models and bounded, labeled input controls |

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

The test suite covers data parsing, reference checking, numeric validation, hashing and collisions, linked-list ordering, AVL rotations and duplicate keys, sorting correctness, heap ordering, search filters, recommendation scoring, comparable valuation, algorithm comparison, and all eight CLI options in one scripted session. Tasks 5–6 add table-model tests, bounded input tests, headless Swing panel interactions, reset/empty-result cases, exact ID lookup, recommendation and valuation UI, and button contrast checks. The combined suite contains **85 JUnit test methods** as of Task 6.

Run `mvn clean test`; Maven Surefire prints the exact number of executed JUnit tests. Do not treat a compilation-only run or `-DskipTests` as a completed validation. The CLI can also be exercised manually using `mvn exec:java`.

## Current limitations

- Data is synthetic and read-only; no live market feed or persistence of edits.
- Recommendations use a fixed scoring formula rather than learned personal preferences.
- Comparable valuation averages a small set of related past sales; it does not estimate uncertainty or market dynamics.
- Benchmark readings are illustrative micro-timings. Production performance investigation would use larger controlled datasets and JMH.
- The desktop interface is read-only and targets regular desktop window sizes (tested at 1000×650 and larger); it is not designed for mobile screens.

## Development roadmap

- [x] Task 1 — Data models, CSV reader, linked list, hash table, base CLI
- [x] Task 2 — AVL price index, property filters, Merge Sort, Quick Sort
- [x] Task 3 — Max Heap, recommendations, comparable valuation
- [x] Task 4 — Complete CLI, validation, integration tests, algorithm comparison
- [x] Task 5 — Swing dashboard, search and sale history
- [x] Task 6 — Swing recommendations, valuation, algorithm comparison and readable button styling
- [ ] Task 7 — Final technical documentation, screenshots and report
