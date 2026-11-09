# Property Intelligence Engine — DSA Edition

**Task 3 complete:** Java 21 CLI application with CSV loading, custom linked list, hash table, AVL price index, filtering, Merge Sort, Quick Sort, **custom max heap**, **property recommendations**, and **comparable-sale valuation**.

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
PROPERTY INTELLIGENCE ENGINE - TASK 3
1. Dataset summary
2. Browse properties
3. Find property by ID
4. View sale history
5. Search, filter and sort properties
6. Recommend properties
7. Estimate a property value
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

**For existing Task 2 users:** Extract `PropertyIntelligence-Task3-Update.zip` over your project. It contains only new/changed Java files, tests and this README. It does **not** include `data/`, and therefore does not overwrite CSV files, Git history, or local settings.

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

All property lookups, sale histories, price filtering, and sorting from Tasks 1–2 remain functional.

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

## Task 3 — heap-based recommendations and valuation

Task 3 adds two independent services on top of the existing in-memory `PropertyStore`. The services do not change property or sale records and do not require a database, machine-learning library, or network connection.

### New Java classes

| Path (under `src/main/java/edu/shu/propertyintel/`) | Purpose |
|---|---|
| `dsa/MaxHeap.java` | Generic array-backed binary max heap with insertion, peek, removal and resizing |
| `service/RecommendationCriteria.java` | Validated filters for a recommendation request |
| `service/PropertyRecommendation.java` | One ranked property with its last sale price and score |
| `service/RecommendationService.java` | Filters properties, calculates transparent preference scores, and extracts Top-K results from a max heap |
| `service/ComparableProperty.java` | A comparable property, last sale price and relative similarity index |
| `service/ValuationResult.java` | Read-only valuation result with an immutable comparable list |
| `service/ComparableValuationService.java` | Selects same-type comparables with a heap and calculates a simple average |
| `cli/ConsoleApplication.java` | Adds menu options 6 and 7; existing options remain unchanged |

### How the custom max heap works

The max heap is stored in a dynamically resized array. For an element at index `i`:

- Parent index: `(i - 1) / 2`
- Left child: `2 * i + 1`
- Right child: `2 * i + 2`

`add` inserts at the end and moves a value upwards while it outranks its parent (*sift up*). `poll` returns the root, moves the last element to the root, then moves it downward until the heap order is restored (*sift down*). A comparator defines what counts as the highest-ranked element; the heap itself works for any non-null element type.

| Heap operation | Worst-case time |
|---|---|
| `peek()` | O(1) |
| `add()` | O(log n), excluding an occasional O(n) array resize |
| `poll()` | O(log n) |
| Heap storage | O(n) |

The heap is deliberately used for both recommendation ranking and comparable-property selection. We do not call Java's built-in `PriorityQueue`.

### Option 6 — recommend properties

Enter a **maximum budget** (required), minimum bedrooms, minimum area in square metres, and optional property type/neighborhood. Properties must satisfy these filters, and they must have a recorded sale price. Neighborhoods accept either their IDs or names.

```text
Choose an option: 6
Maximum budget: 700000
Minimum bedrooms [0]: 3
Minimum area in sqm [0]: 150
Property type (optional): House
Neighborhood name or ID (optional):
```

For eligible properties, the ranking score adds up to **100 possible points**:

| Criterion | Maximum points | Method |
|---|---:|---|
| Affordability | 25 | `(budget - latestPrice) / budget`, multiplied by 25 |
| School score | 20 | Neighborhood's school score / 10, multiplied by 20 |
| Safety score | 20 | Neighborhood's safety score / 10, multiplied by 20 |
| Transit score | 10 | Neighborhood's transit score / 10, multiplied by 10 |
| Area | 15 | Area divided by `max(150, 1.5 × minimumArea)`, capped at 1 |
| Parking | 10 | Up to 2 spaces, worth 5 points each |

A higher score means a stronger match **under this specific hand-designed scoring rule**, not an ML probability or objective measure of real-estate quality. When scores are equal, property ID determines display order. Eligible properties enter a max heap; the service extracts at most five highest-ranked results.

### Option 7 — comparable-property valuation

Select an existing property ID. The program:

1. Finds the subject property using the custom hash table.
2. Examines other properties of the **same property type** that have a recorded sale.
3. Computes a comparison penalty from differences in bedrooms, area, bathrooms, parking spaces and neighborhood.
4. Converts it to a relative similarity index: `1 / (1 + penalty)`.
5. Uses the max heap to select up to **three most similar** comparables.
6. Reports the **arithmetic mean** of those comparable properties' latest sale prices.

The comparison penalty weights are: 4 per bedroom difference; 6 times relative area difference; 0.75 per bathroom difference; 0.5 per parking-space difference; and 1.5 if the neighborhood differs. The similarity index is for relative ranking **only**; it is not a probability or validated prediction accuracy. If no same-type sold properties exist, the service reports that no comparable estimate can be made. When fewer than three are available, it uses the ones it has.

```text
Choose an option: 7
Property ID to value: P0005
Estimated value of P0005: 605,000.00
Based on 3 comparable property sale(s):
P0008  House  534,000.00  relative match 0.197
P0010  House  605,000.00  relative match 0.135
P0012  House  676,000.00  relative match 0.128
```

**Valuation limitations:** This is an illustrative DSA method, not a trained model, market appraisal, confidence interval, or reliable quote. The bundled data is synthetic, and the comparable prices are historical observations. Do not use these estimates for financial decisions.

### Complexity of Task 3 services

With `n` properties and a requested result limit of `k`:

| Operation | Complexity |
|---|---|
| Score/filter all properties | O(n) |
| Build max heap through inserts | O(n log n) |
| Extract the top `k` results | O(k log n) |
| Overall recommendation | O(n log n + k log n) time and O(n) heap storage |
| Comparable selection and valuation | O(n log n + k log n) time and O(n) heap storage |

Because the dataset is intentionally small, we prioritize clear DSA implementation. A bounded heap could reduce Top-K memory to O(k), but it is not necessary for this semester scope.

### Tests added in Task 3

- Max heap: ordering, duplicates, dynamic resizing, custom comparator, null/empty behavior.
- Recommendations: filtering, top limit, deterministic ties, eligibility, invalid inputs, score bounds.
- Valuation: closest comparable selection, same-type restriction, missing sales, insufficient comparables, invalid property IDs.
- CLI: both new menu options, error handling and clean return to the main menu.

The combined suite includes **38 JUnit test methods** across 13 test classes. Run `mvn clean test` on your Mac. The full suite was also checked locally using a temporary JUnit-compatible runner because Maven is unavailable in the build environment; local compilation and that runner passed all 38 checks. The official Maven result must still be confirmed on your machine.

## Task roadmap

- [x] Task 1: foundation, models, CSV, linked list, hash table, CLI
- [x] Task 2: AVL tree, price-range search, multi-filter search, merge and quick sort
- [x] Task 3: max heap, recommendations, comparable-property valuation
- [ ] Task 4: complete CLI integration and algorithm analysis
- [ ] Task 5: Swing dashboard/search/history
- [ ] Task 6: Swing recommendation/valuation and packaging
- [ ] Task 7: documentation/report and viva preparation
