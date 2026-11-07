# Property Intelligence Engine — DSA Edition

**Task 1: Java data loading, custom linked list, custom hash table and working CLI.**

## Requirements

- Java JDK 21
- Apache Maven 3.9 or later

## Run

From the project root:

```bash
mvn clean test
mvn exec:java
```

You can provide the data directory explicitly if running the compiled class manually:

```bash
java -cp target/classes edu.shu.propertyintel.Main data
```

## Replace the included sample data

The `data/` folder contains **12 illustrative synthetic properties, 24 sales and 6 neighborhoods** so the project runs immediately. They are **not** the complete original repository's CSV files. For the full dataset, replace all three CSV files with those found at:

https://github.com/Khalitus/applied-agentic-ai-projects/tree/main/property-intelligence-engine/data

Keep their filenames (`properties.csv`, `sales.csv`, `neighborhoods.csv`) and original column names. The code loads either dataset without modification.

## CLI options

1. Dataset summary
2. Browse property records
3. Lookup one property by ID (`P0001`)
4. View a property's historical sales, chronological changes and percentages
0. Exit

Task 2 will add AVL search/filtering and sorting. Task 3 will add recommendations/valuation. Task 4 will finish the CLI. Tasks 5–6 add Swing. Task 7 covers documentation.

## Source layout

- `model`: Java domain records
- `dsa`: custom linked list and custom hash table (separate chaining, growth)
- `data`: CSV parser, CSV loader and in-memory property repository
- `cli`: user interaction, read-only lookup and history display
- `Main`: application entry point

## Algorithm complexity

| Operation | Complexity |
|---|---|
| Linked list append | O(1) |
| Linked list sorted insertion | O(n) |
| Linked list traversal | O(n) |
| Hash table lookup/insert | O(1) average, O(n) worst-case |
| Hash table resize | O(n), amortized insertion O(1) average |
| Find latest sale | O(1) after sales are loaded chronologically |

Sales are inserted chronologically per property, making loading each sale O(h), where h is that property's current history length. This is simple and sufficient for the course dataset size.
