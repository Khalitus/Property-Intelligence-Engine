# Property Intelligence Engine — DSA Edition

**Task 2 complete:** Java 21 command-line application with CSV loading, custom linked list, custom hash table, **AVL price index**, property filtering, **Merge Sort**, and **Quick Sort**.

The app is built using Java 21 and Maven, and its core DSA operations are implemented by this project rather than using `TreeMap` or `Collections.sort`.

## Requirements

- JDK 21
- Maven 3.9+
- Terminal in the project directory (`pom.xml` at the root)

## Start

```bash
mvn clean test
mvn exec:java
```

If macOS/external-SSD metadata files (`._*`) interfere with test discovery:

```bash
mvn clean test-compile
find target -type f -name '._*' -delete
mvn surefire:test
```

The `pom.xml` also has an exclusion for `._*` test class names to address this issue.

Run without Maven after compilation:

```bash
java -cp target/classes edu.shu.propertyintel.Main data
```

An optional positional argument to `Main` can specify a different CSV directory.

## Menu

```text
PROPERTY INTELLIGENCE ENGINE - TASK 2
1. Dataset summary
2. Browse properties
3. Find property by ID
4. View sale history
5. Search, filter and sort properties
0. Exit
```

### Sample search

Choose option `5`. The following is a sample sequence (each answer is on a new line):

```text
Property type (e.g. House): House
Neighborhood name or ID: Midtown
Minimum bedrooms: 3
Minimum latest sale price: 200000
Maximum latest sale price: 800000
Sort by: 1=Price, 2=Area, 3=Bedrooms, 4=Year built [1]: 1
Algorithm: 1=Merge Sort, 2=Quick Sort [1]: 2
Order: 1=Ascending, 2=Descending [1]: 1
Show up to how many results (1-50) [20]: 10
```

Every filter is optional; leaving it blank includes all values for that field. For sort field, algorithm, order, and result limit, a blank entry selects the default shown in brackets. Both neighborhood **names and IDs** work; matches are case-insensitive. Search reports the **total number of matches** and displays up to the selected number of rows.

The price filter is inclusive and uses each property's **most recent sale by date**. Properties without a recorded sale cannot satisfy a price filter; they still appear in searches without price bounds and sort last when sorting by price.

## Dataset

The bundled CSV files contain **12 illustrative properties, 24 sales, and 6 neighborhoods**. The application is compatible with the larger *original synthetic dataset* from:

https://github.com/Khalitus/applied-agentic-ai-projects/tree/main/property-intelligence-engine/data

To use that dataset, replace these three files with the originals (same column names):

```text
data/properties.csv
data/sales.csv
data/neighborhoods.csv
```

**For existing Task 1 users:** The separate `PropertyIntelligence-Task2-Update.zip` contains only new/changed source, tests, `pom.xml`, and this README. Extracting it over your Task 1 directory does **not** overwrite your `data/` files.

## Architecture

```text
CSV files
   |
   v
DatasetLoader --> PropertyStore (hash tables + sale-history linked lists)
                      |
                      v
              PropertySearchService
                  /        \
      AVLTree<Double,Property>   All properties
           (price filter)       (no price filter)
                  \        /
                   v      v
              Additional filters
                      |
                      v
               Property[] array
                      |
             Merge Sort / Quick Sort
                      |
                      v
                ConsoleApplication
```

All property lookups and sale histories from Task 1 remain functional.

## New files

| Path (relative to `src/main/java/edu/shu/propertyintel/`) | Purpose |
|---|---|
| `dsa/AVLTree.java` | Generic AVL tree; equal price keys store multiple values in a custom linked list; inclusive range queries |
| `algorithm/MergeSort.java` | Generic stable merge sort |
| `algorithm/QuickSort.java` | Generic 3-way quicksort with median-of-three pivot |
| `service/SearchCriteria.java` | Optional search filters and input validation |
| `service/SortField.java` | Supported sort fields |
| `service/SortAlgorithm.java` | Selection between custom sorting algorithms |
| `service/PropertySearchService.java` | Builds price index, filters candidates and sorts results |
| `cli/ConsoleApplication.java` (updated) | New search/filter/sort menu option |

Existing model classes, CSV parser, linked list, hash table, and data store are retained.

## How the AVL index works

1. The service is created after all three CSVs have loaded into `PropertyStore`.
2. It visits each property and takes the latest recorded sale price, if available.
3. It inserts `price -> property` into the AVL tree.
4. Multiple properties can have exactly the same price, so each tree node contains a `CustomLinkedList<Property>` (not just one property).
5. `rangeSearch(minPrice, maxPrice)` prunes subtrees whose keys cannot fall within the requested range.
6. Other filters (property type, neighborhood, bedrooms) are applied to the resulting candidates.

Each tree node remembers its `height`. After a recursive insertion, heights and balance factors are updated, and the appropriate rotation is selected:

| Case | Shape of insertion | Correction |
|---|---|---|
| LL | Left child, then left | Right rotation |
| RR | Right child, then right | Left rotation |
| LR | Left child, then right | Left rotation on child; right rotation on parent |
| RL | Right child, then left | Right rotation on child; left rotation on parent |

The AVL index is built **once from the fully loaded dataset** and used for read-only searching in this task. If your application later supports changing sale prices while it runs, it must update or rebuild that index. Task 2 intentionally does not implement editing properties or sales.

## Sorting

- **Merge Sort:** recursively splits the array, then merges sorted halves. Equal elements preserve their original order when a comparator reports `0` (stable). Uses an auxiliary array.
- **Quick Sort:** selects a median-of-three pivot and partitions values into less-than, equal-to, and greater-than groups. Sorting the smaller partition recursively helps keep call-stack depth limited.
- Supported sort fields: latest sale **price**, **area**, **bedrooms**, **year built**.
- Direction: ascending or descending. For equal values, property ID provides deterministic tie-breaking. Properties with no sale always sort after priced properties when sorting by price, regardless of direction.

The application does **not** use `Collections.sort()` or `Arrays.sort()` in its implementation. Tests may compare the output against Java's reference sort.

## Complexity summary

Let `n` be the total number of properties, `u` the number of distinct latest sale prices, `c` the number of properties satisfying the indexed price range, and `r` the number of final matching properties.

| Operation | Complexity |
|---|---|
| Construct AVL price index | O(n log u) with balanced tree insertion, plus linked-list append for duplicate keys |
| AVL insertion | O(log u) |
| Find specific price | O(log u + d) where `d` is the number of properties at that price |
| Inclusive price range search | O(log u + c) typical for a balanced tree and ordered output |
| Filtering without price bounds | O(n) |
| Filtering after AVL range retrieval | O(c) |
| Merge Sort | O(r log r) time, O(r) extra space |
| Quick Sort | O(r log r) average, O(r²) worst time; O(log r) recursion stack due to smaller-side recursion |
| Property lookup by ID (Task 1) | O(1) average, O(n) worst-case collision behavior |

All search results are sorted using the requested algorithm, so the total query cost includes both filtering and sorting. The goal is correct, explainable DSA rather than claiming that every filter can be indexed in constant time.

## Tests

JUnit 5 tests reside in `src/test/java` and cover:

- Task 1 regression: CSV quoting, CSV validation, source data linking, custom linked list, custom hash table, previous CLI menu
- AVL rotations: LL/RR/LR/RL; sorted range output; same-price duplicates; empty and null inputs; balancing with 1,000 sequential keys
- Sorting: duplicates; ascending/descending; empty/single/already-sorted arrays; 5,000 randomized values; merge stability
- Property search: combined filters; exact inclusive boundaries; use of latest sales; unsold properties; all four sort fields; invalid criteria
- New CLI: descending quicksort, validation and clean exit

There are **24 annotated JUnit test methods** across 9 test classes. On your Mac, run `mvn clean test` or the SSD-safe three-command sequence above and verify `Tests run: 24, Failures: 0, Errors: 0`.

## Task roadmap

- [x] Task 1: foundation, models, CSV, linked list, hash table, CLI
- [x] Task 2: AVL tree, price-range search, multi-filter search, merge and quick sort
- [ ] Task 3: max heap, recommendations, comparable-property valuation
- [ ] Task 4: complete CLI integration and algorithm analysis
- [ ] Task 5: Swing dashboard/search/history
- [ ] Task 6: Swing recommendation/valuation and packaging
- [ ] Task 7: documentation/report and viva preparation
