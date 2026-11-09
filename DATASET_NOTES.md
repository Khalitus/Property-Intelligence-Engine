# Property Intelligence Engine — Expanded Synthetic Dataset

This is a **synthetic demonstration dataset**, not actual property transactions, a real housing-market sample, or a verified currency-based valuation source. It is designed to exercise the existing Java DSA application's property lookup, AVL price indexing, historical sales, sorting, recommendations, comparable valuation, and algorithm comparisons.

## Contents

| File | Records | Structure |
|---|---:|---|
| `data/properties.csv` | 1,500 | One row per property |
| `data/sales.csv` | 4,751 | Chronologically varied sale events, across the properties |
| `data/neighborhoods.csv` | 12 | Locality-level scores and distance indicators |

There are **1,471 properties with sale records** and **29 intentionally without recorded sales**, to exercise missing-history handling. Among sold properties, 94 have one sale; 1,377 have two to five sales. Recorded sales span 2011–2026. Dates are no later than October 2026.

## Compatibility

The field names and types match `DatasetLoader.java` in Property Intelligence Engine (Java 21, Tasks 1–6). No Java code changes are required.

**Property fields**: `property_id, neighborhood_id, property_type, bedrooms, bathrooms, area_sqm, year_built, parking_spaces, floor, furnished, amenities, description`

**Sales fields**: `sale_id, property_id, sale_date, sale_price`

**Neighborhood fields**: `neighborhood_id, neighborhood_name, distance_to_center_km, school_score, transit_score, safety_score`

IDs range from `P0001` to `P1500`; existing `N01`–`N06` localities and their scores were retained, with `N07`–`N12` added. Sales use unique IDs and valid property references. Dates are ISO `YYYY-MM-DD`, and `furnished` is encoded as 0/1.

Property types include Apartment, House, Townhouse, Villa, and Studio. Prices are in **unspecified synthetic monetary units** and should not be labeled USD, PKR, or any other actual currency. The sample is not geographically anchored to any real city.

## How the records were generated

- Deterministic generator seed: `20261109`.
- Building type influences floor, bedrooms, bathrooms, space, parking, and available amenities.
- The location, floor area, property type, construction year, property condition, parking, and furnishings influence an illustrative underlying property price.
- Historical sale prices reflect an approximate synthetic annual trend, with bounded random variation. They are not historical observations or a forecast.
- Multiple sales for a property are separated by at least two calendar years, and every sale occurs after the recorded build year.
- Rows in `sales.csv` are deliberately unsorted, demonstrating the app's custom linked-list chronological insertion.
- Monetary prices are rounded to the nearest 500; duplicate latest-sale prices are expected, exercising the AVL duplicate-key behavior.

## Install into your existing project

In your project directory, first preserve the current sample files outside the repository:

```bash
cd "/Volumes/SSD/Projects/Property Intelligence Engine"
cp -R data ../PropertyIntelligence-data-backup
```

Then extract the ZIP **from the project root** (with the ZIP in Downloads):

```bash
unzip -o ~/Downloads/PropertyIntelligence-LargeDataset.zip -d .
```

Run the tests and desktop UI:

```bash
mvn clean test
mvn -Pdesktop exec:java
```

Or use the original CLI:

```bash
mvn exec:java
```

In Dashboard / Dataset Summary you should see **1,500 properties; 4,751 sales; 12 neighborhoods; 1,471 properties with recorded sales**. To try a property lookup, use `P0001` or `P1000`.

The app's JUnit tests use their own fixtures, so replacing the CSV dataset should not alter their assertions. If you encounter the known external-SSD macOS `._` metadata issue with Maven test discovery, use your existing clean/test-compile/metadata-removal/Surefire sequence.

## Reproduction

The generator is included at `tools/generate_dataset.py`. It uses only the Python 3 standard library and writes the same three files under `data/`:

```bash
python3 tools/generate_dataset.py
```

The generator is not used by the Java application at runtime; the Java application reads the CSV files directly.

## Limitations

These values are fictional and partly determined by chosen simulation formulas. They can demonstrate code behavior, algorithmic differences, index balancing, and recommendation/valuation **logic**, but they cannot validate real-world accuracy or support factual market conclusions. Benchmark times are environment-dependent, not guaranteed speedups.

## Application valuation policy (pre-documentation checkpoint)

The Java valuation service uses only same-type comparables whose **latest** sale is on or after the inclusive cutoff three years before the newest sale anywhere in these CSVs. For this dataset, the newest sale is **2026-10-27**, giving a cutoff of **2023-10-27**. Fewer than the requested number of comparables may be returned. Historic sales older than the cutoff remain accessible from the Sale History view. Recorded sale dates appear with prices in the desktop tables and CLI. No inflation or time adjustment is applied.
