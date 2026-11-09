# Testing and verification

## What is being verified

The application is a read-only, data-backed Java DSA system, so verification focuses on correct parsing and relationships, correct custom algorithms, stable service behavior, CLI flows and bounded Swing interactions. **Passing compilation is not the same as running the test suite.**

The committed suite consists of **96 JUnit 5 test methods**, present in the final CLI and GUI revision. It includes targeted fixtures and a full-dataset integration class. The baseline local check passed all **96/96** methods; the release check should be rerun after applying the documentation-only update.

## Run tests

From the repository root, with Java 21 and Maven 3.9+:

```bash
java -version
mvn -version
mvn clean test
```

A successful run should finish with zero failures and errors. Maven Surefire generates detailed machine-readable and text reports in `target/surefire-reports/`. Check these files if the terminal shows an error.

### macOS external-SSD metadata

On external Mac drives, `._*` AppleDouble files can be mistaken for test classes during discovery. If you encounter an invalid class such as `._ConsoleApplicationTest`, compile and clean generated metadata **before** invoking Surefire:

```bash
mvn clean test-compile
find target -type f -name '._*' -delete
mvn surefire:test
```

The `pom.xml` also configures exclusions for `**/._*` and inner classes. This workaround does not skip JUnit tests.

## Coverage by component

| Area | Tests / checks in the repository | Representative cases |
|---|---|---|
| CSV and repository | `CsvReaderTest`, `DatasetLoaderTest`, `PropertyStoreValidationTest` | quoted/multiline fields, invalid numbers, foreign-key references, duplicated IDs |
| Linked list | `CustomLinkedListTest` | ordered append, sorted insertion, duplicate order |
| Hash table | `CustomHashTableTest` | collisions, key replacement, repeated resize and rehash |
| AVL | `AVLTreeTest` | LL/LR/RL/RR rotations, equal-price nodes, inclusive range bounds |
| Sorting | `SortingAlgorithmsTest` | empty/single arrays, stable Merge Sort, randomized reference comparisons |
| Heap | `MaxHeapTest` | priority order, resize, comparator and invalid operations |
| Search and ranking | `PropertySearchServiceTest`, `RecommendationServiceTest` | combined filters, no-sale handling, budget constraints, deterministic ties |
| Valuation | `ComparableValuationServiceTest`, `ValuationRecencyTest` | same-type selection, date cutoff, missing comparables, zero-sales dataset |
| Benchmarking | `AlgorithmComparisonServiceTest` | result equivalence, missing prices, identical price keys |
| CLI | `ConsoleApplicationTest`, `PropertySearchCliTest`, `PropertyIntelligenceCliTest`, `CompleteCliTest` | input validation and all eight menu choices in one session |
| Swing models and controls | `*TableModelTest`, `InputRulesTest`, `PanelInteractionTest`, `IntelligencePanelsTest`, `ButtonContrastTest`, `SaleDateTransparencyTest` | filters, reset behavior, date columns, visible button text and empty states |
| Committed dataset | `ExpandedDatasetIntegrationTest` | final counts, references, price index coverage, recommendations, valuation and benchmark |

## Full-dataset integrity baseline

The committed CSVs are expected to produce:

| Assertion | Expected |
|---|---:|
| Properties loaded | 1,500 |
| Sales loaded | 4,751 |
| Neighborhoods loaded | 12 |
| Properties with sale history | 1,471 |
| Properties without history | 29 |
| Latest sale date | 2026-10-27 |
| Inclusive comparable cutoff | 2023-10-27 |

`ExpandedDatasetIntegrationTest` loads `data/`, checks references and counts, asserts that each sale's year is not earlier than the property's build year, ensures all sold properties are indexed, and runs representative recommendations, valuation and algorithm-comparison workflows. **It assumes that the committed dataset has not been replaced by the 12-row starter sample.**

## Manual desktop verification

Automated tests do not replace visual inspection on the actual target Mac. The following is a reproducible manual regression pass after launching `mvn -Pdesktop exec:java`:

| Screen | Action | Expected behavior |
|---|---|---|
| Dashboard | Start with committed CSVs | 1,500 properties, 4,751 sales, 12 neighborhoods; indexed count 1,471 |
| Properties | Filter prices `350000` to `800000` and Search | Matching property rows, 753 results on the committed dataset; selected row shows details |
| Properties | Enter `P0001` in Find by ID | Exact record lookup independently of other filters |
| Properties | Enter min price above max price | Validation message; no crash |
| History | Enter `P0001` | Chronological dated sales with changes |
| History | Enter an unknown ID | Clear error, no stale results |
| Recommendations | Use initial budget; select a row | Up to five ranked matches, explanation, latest sale date |
| Recommendations | Set contradictory/restrictive criteria | Useful no-results message |
| Valuation | Enter `P0001`, comparables `3` | Up to three same-type recent sales, dated rows and mean estimate |
| Valuation | Unknown property ID | No estimate; actionable explanation |
| Algorithms | Run comparison twice | Three comparison rows; interface remains responsive; times may differ |
| Navigation | Switch between pages | Sidebar selection stays legible; prior page state remains usable |
| Layout | Resize to ~1000 × 650 | Main actions remain visible, tables scroll; no hidden critical labels |
| Platform | Inspect light/dark macOS appearance | Button foreground/background contrast remains readable |

### Screenshot evidence

All six `docs/screenshots/*.png` images were captured from the **running Java 21 Swing application**, loaded with the submitted **1,500-property** dataset, in a **Linux Xvfb test session (1340 × 850)**. These are *not* fabricated graphical mockups. macOS-native styling must still be reviewed independently; screenshots cannot prove exact native rendering.

## What algorithm timings do and do not show

`AlgorithmComparisonService` verifies paired results, performs **two warm-up and five measured trials**, and reports the median microseconds per operation. It does not control JIT compilation, garbage collection, processor frequency or scheduler noise. No timing figures are fixed in the documentation because measured values vary. Theoretical complexity is explained in [ALGORITHMS.md](ALGORITHMS.md).

## Reproduction and release checklist

- [ ] `mvn clean test` reports **96** tests, zero failures and zero errors, on the release machine.
- [ ] Desktop application opens and all six screens function on macOS.
- [ ] CLI opens and exits with `0`.
- [ ] Committed `data/` files produce the documented counts.
- [ ] Recorded sale dates appear in recommendation and valuation tables.
- [ ] No `target/`, local IDE settings, runtime caches or unrelated backup files are staged.
- [ ] Screenshot images and diagrams are readable in GitHub Markdown preview.
- [ ] `git status` is clean after the documentation commit.

## Known limitations

The data is synthetic and not suitable for assessing real prediction accuracy. The valuation uses a deterministic comparable heuristic and no price time adjustment. Recommendations describe historical records, not market availability. GUI tests cover UI behavior but are not a substitute for manual native-macOS inspection. Runtime microbenchmarks are educational illustrations, not formal performance measurements.
