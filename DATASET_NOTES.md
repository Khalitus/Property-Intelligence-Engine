# Dataset specification and provenance

The dataset in `data/` is **fictional and deterministically generated** for demonstrating the Property Intelligence Engine's data structures, algorithms and UI. It is **not** a sample of verified property listings, market transactions, or real neighborhood measurements. Prices are recorded in **unspecified synthetic units**, not PKR, USD, or any other currency.

## Committed contents

| File | Records | Primary ID | References |
|---|---:|---|---|
| `data/properties.csv` | 1,500 | `property_id` | `neighborhood_id` → neighborhoods |
| `data/sales.csv` | 4,751 | `sale_id` | `property_id` → properties |
| `data/neighborhoods.csv` | 12 | `neighborhood_id` | — |

**1,471 properties have recorded transactions; 29 intentionally have none.** Among properties with sales, 94 have one sale and 1,377 have between two and five. Dates span **2011–2026**. The newest transaction is dated **2026-10-27**. Property IDs run from `P0001` through `P1500`.

## Exact CSV schemas

**Properties:**

```text
property_id,neighborhood_id,property_type,bedrooms,bathrooms,area_sqm,year_built,parking_spaces,floor,furnished,amenities,description
```

**Sales:**

```text
sale_id,property_id,sale_date,sale_price
```

**Neighborhoods:**

```text
neighborhood_id,neighborhood_name,distance_to_center_km,school_score,transit_score,safety_score
```

The `furnished` field uses `0`/`1`; sale dates are ISO `YYYY-MM-DD`. Values may be quoted using standard CSV rules. `CsvReader` and `DatasetLoader` implement and validate these exact headers.

The five property types are **Apartment, House, Townhouse, Villa, Studio**. Neighborhood IDs include the original `N01`–`N06` and additional `N07`–`N12`. Location names are fictional for this demonstration; the dataset is not mapped to real geographic coordinates.

## Generation method

The source is `tools/generate_dataset.py`; it uses the **Python 3 standard library** and random seed **`20261109`**. The resulting values are correlated rather than independently random:

- Property type influences area, layout, floor, rooms, parking and amenities.
- Neighborhood scores, property features and construction age influence an illustrative underlying price.
- Historical transactions apply a synthetic annual price trend and bounded variation; they are **not** historical observations or a forecast.
- A property can have one or multiple dated sales, with at least two years between generated transactions.
- Sales are intentionally shuffled in `sales.csv`, demonstrating chronological linked-list insertion.
- Prices are rounded to the nearest 500, producing repeated keys that exercise AVL duplicate-price handling.
- Several properties deliberately have **no sale records**, testing missing-history and missing-price paths.

To regenerate (this **overwrites** the committed CSV files):

```bash
python3 tools/generate_dataset.py
```

Review changes with `git diff -- data/` before committing a regeneration. The Java application **never executes the generator**; it loads the existing CSV files directly.

## Data quality checks

`ExpandedDatasetIntegrationTest` asserts the expected counts and relationships, checks that every sale refers to an existing property, verifies sales do not predate the property's construction year, verifies the 1,471-property AVL index coverage, and exercises recommendations, valuation and algorithm comparison.

Run from the repository root:

```bash
mvn clean test
```

`PropertyStore` also rejects duplicate IDs, unknown references and invalid numeric fields when loading data. The generator deliberately includes duplicate **price values** (valid) but does not create duplicate **record IDs** (invalid).

## How application features interpret prices

- **Property search:** filters on **latest recorded sale price**, not listing price. Properties without sales remain searchable when price is unrestricted.
- **Sale history:** presents transactions chronologically, with changes between adjacent sales.
- **Recommendations:** use latest recorded sale prices for budget eligibility and preference scoring; results are **not currently available listings**.
- **Comparable valuation:** chooses same-type properties whose latest sale is within three years of the **newest sale in the loaded dataset**. For this dataset, the **inclusive cutoff is 2023-10-27**. It computes the unadjusted mean of selected sale prices; it does **not** correct for sale date, inflation, neighborhood market differences or uncertainty.
- **Benchmarks:** compare algorithms on the loaded records, without treating synthetic timing results as evidence of real-world market performance.

The dataset is suitable for **reproducible DSA behavior and integration testing**, not for a factual study of property prices or the accuracy of a financial estimate.
