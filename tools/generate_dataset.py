#!/usr/bin/env python3
"""Generate a reproducible synthetic dataset for Property Intelligence Engine.

No third-party dependencies. Run from any working directory:
    python3 tools/generate_dataset.py
"""
from __future__ import annotations

import csv
from collections import Counter
from pathlib import Path
import random

SEED = 20261109
PROPERTY_COUNT = 1500
OUTPUT_DIR = Path(__file__).resolve().parents[1] / "data"
RNG = random.Random(SEED)

NEIGHBORHOODS = [
    ("N01", "Riverside", 4.2, 8.0, 7.0, 8.0, 1.08),
    ("N02", "Midtown", 2.7, 9.0, 9.0, 7.0, 1.20),
    ("N03", "Central", 1.0, 8.0, 10.0, 7.0, 1.30),
    ("N04", "Lakeside", 7.5, 9.0, 6.0, 9.0, 1.25),
    ("N05", "Green Park", 6.1, 7.0, 7.0, 9.0, 1.12),
    ("N06", "Old Town", 5.4, 6.0, 6.0, 7.0, 0.89),
    ("N07", "Harbor Point", 8.4, 7.6, 6.9, 8.1, 1.04),
    ("N08", "Westfield", 10.8, 7.8, 6.2, 8.3, 0.92),
    ("N09", "University Quarter", 3.6, 9.4, 8.8, 7.6, 1.22),
    ("N10", "East Gardens", 9.3, 8.5, 6.4, 8.7, 1.06),
    ("N11", "Hillcrest", 12.8, 8.7, 5.3, 9.3, 1.18),
    ("N12", "South Market", 5.9, 7.2, 8.6, 6.8, 0.95),
]

PROPERTY_COLUMNS = [
    "property_id", "neighborhood_id", "property_type", "bedrooms", "bathrooms",
    "area_sqm", "year_built", "parking_spaces", "floor", "furnished",
    "amenities", "description",
]
SALE_COLUMNS = ["sale_id", "property_id", "sale_date", "sale_price"]
NEIGHBORHOOD_COLUMNS = [
    "neighborhood_id", "neighborhood_name", "distance_to_center_km",
    "school_score", "transit_score", "safety_score",
]

TYPES = ["Apartment", "House", "Townhouse", "Villa", "Studio"]
TYPE_WEIGHTS = [41, 28, 17, 9, 5]
TYPE_SPEC = {
    "Apartment": ((1, 4), (44, 56), 1.02),
    "House": ((2, 6), (48, 66), 1.00),
    "Townhouse": ((2, 5), (42, 52), 1.05),
    "Villa": ((3, 7), (60, 80), 1.29),
    "Studio": ((0, 1), (31, 49), 0.94),
}


def write_csv(name: str, headers: list[str], rows: list[dict]) -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    with (OUTPUT_DIR / name).open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=headers, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def sample_property(pid: int) -> tuple[dict, float]:
    neighborhood = RNG.choice(NEIGHBORHOODS)
    neighborhood_id, name, distance, school, transit, safety, multiplier = neighborhood
    kind = RNG.choices(TYPES, weights=TYPE_WEIGHTS, k=1)[0]
    bed_range, area_per_bed_range, kind_premium = TYPE_SPEC[kind]
    bedrooms = RNG.randint(*bed_range)
    if kind == "Studio":
        bedrooms = 0
        bathrooms = 1
        area = round(RNG.uniform(25, 63), 1)
    else:
        bathrooms = max(1, min(7, bedrooms + RNG.choice([-1, 0, 0, 0, 1])))
        area = round((max(1, bedrooms) * RNG.uniform(*area_per_bed_range)
                      + RNG.uniform(15, 50) if kind != "Villa" else
                      bedrooms * RNG.uniform(52, 78) + RNG.uniform(70, 135)), 1)
    year_built = int(RNG.triangular(1968, 2025, 2006))
    if kind in {"Apartment", "Studio"}:
        floor = RNG.choices([0, 1, 2, 3, 4, 5, 8, 12, 16, 22],
                            [4, 10, 12, 14, 13, 10, 12, 12, 8, 5], k=1)[0]
        parking = RNG.choices([0, 1, 2], [37, 51, 12], k=1)[0]
    else:
        floor = RNG.choice([0, 1, 1, 2, 2, 3])
        parking = RNG.choices([0, 1, 2, 3, 4], [9, 32, 38, 16, 5], k=1)[0]
    furnished = RNG.random() < (0.46 if kind in {"Apartment", "Studio"} else 0.25)
    amenities = set()
    if parking:
        amenities.add("parking")
    if kind in {"Apartment", "Studio"} and floor >= 4:
        amenities.add("elevator")
    if kind in {"Apartment", "Studio", "Townhouse"} and RNG.random() < 0.68:
        amenities.add("balcony")
    if kind in {"House", "Villa", "Townhouse"} and RNG.random() < 0.65:
        amenities.add("garden")
    if RNG.random() < 0.62:
        amenities.add("security")
    if RNG.random() < 0.43:
        amenities.add("storage")
    if RNG.random() < 0.23:
        amenities.add("gym")
    if kind == "Villa" and RNG.random() < 0.48:
        amenities.add("pool")
    if RNG.random() < 0.24:
        amenities.add("rooftop")
    if kind in {"Villa", "House"} and RNG.random() < 0.19:
        amenities.add("solar")
    if not amenities:
        amenities.add("storage")
    amenities = sorted(amenities)
    condition = RNG.choices(
        ["Well-maintained", "Modern", "Renovated", "Quiet", "Bright", "Spacious"],
        weights=[22, 18, 13, 12, 17, 18], k=1,
    )[0]
    amenity_summary = " and ".join(amenities[:2])
    locality = (
        "strong local school access" if school >= 8.5 else
        "good transit options" if transit >= 8 else
        "residential surroundings" if safety >= 8.5 else
        "local services"
    )
    description = (
        f"{condition} {kind.lower()} in {name}, {area:.1f} sqm, "
        f"with {amenity_summary} and {locality}."
    )
    record = {
        "property_id": f"P{pid:04d}",
        "neighborhood_id": neighborhood_id,
        "property_type": kind,
        "bedrooms": bedrooms,
        "bathrooms": bathrooms,
        "area_sqm": f"{area:.1f}",
        "year_built": year_built,
        "parking_spaces": parking,
        "floor": floor,
        "furnished": int(furnished),
        "amenities": "|".join(amenities),
        "description": description,
    }
    # Prices are synthetic dataset units, not amounts in a verified currency.
    # Area, location, building age, type, condition and amenities affect valuation.
    age_discount = max(0.76, 1.0 - (2026 - year_built) * 0.0035)
    site_quality = 1 + 0.012 * (school + safety - 15) + 0.005 * (transit - 7)
    condition_factor = {
        "Well-maintained": 1.01, "Modern": 1.065, "Renovated": 1.095,
        "Quiet": 1.0, "Bright": 1.015, "Spacious": 1.025,
    }[condition]
    unit_price = RNG.uniform(2150, 2850)
    current_value = (
        area * unit_price * multiplier * kind_premium * age_discount *
        site_quality * condition_factor * (1 + parking * 0.017) *
        (1.028 if furnished else 1.0) * RNG.uniform(0.94, 1.06)
    )
    return record, current_value


def historical_years(year_built: int, sales_count: int) -> list[int]:
    first_possible = max(2011, year_built + 1)
    max_count = (2026 - first_possible) // 2 + 1
    count = min(sales_count, max(0, max_count))
    if count == 0:
        return []
    min_latest = first_possible + 2 * (count - 1)
    candidates = list(range(min_latest, 2027))
    # Favor recent transactions, without making every property sell in the same year.
    weights = [1.28 ** (year - min_latest) for year in candidates]
    latest = RNG.choices(candidates, weights=weights, k=1)[0]
    if count == 1:
        return [latest]
    prior_count = count - 1
    # Choose earlier years with at least two years between consecutive sales.
    adjusted_length = (latest - first_possible - 1) - (prior_count - 1)
    positions = sorted(RNG.sample(range(adjusted_length), prior_count))
    earlier = [first_possible + index + i for i, index in enumerate(positions)]
    return earlier + [latest]


def main() -> None:
    property_rows = []
    sale_rows = []
    for pid in range(1, PROPERTY_COUNT + 1):
        record, current_value = sample_property(pid)
        property_rows.append(record)
        requested = RNG.choices([0, 1, 2, 3, 4, 5],
                                weights=[2, 6, 18, 35, 28, 11], k=1)[0]
        years = historical_years(record["year_built"], requested)
        for year in years:
            month = RNG.randint(1, 10 if year == 2026 else 12)
            day = RNG.randint(1, 28)
            historical_value = current_value / (1.049 ** (2026 - year))
            # Independent sale-to-sale variation, with a few occasional declines.
            observed_price = max(20000, historical_value * RNG.uniform(0.95, 1.05))
            observed_price = int(round(observed_price / 500.0) * 500)
            sale_rows.append({
                "sale_id": "",  # IDs assigned after shuffling, preserving uniqueness.
                "property_id": record["property_id"],
                "sale_date": f"{year:04d}-{month:02d}-{day:02d}",
                "sale_price": observed_price,
            })

    # Out-of-order CSV rows exercise the linked list's chronological insertion.
    RNG.shuffle(sale_rows)
    for i, row in enumerate(sale_rows, 1):
        row["sale_id"] = f"S{i:05d}"

    neighborhood_rows = [
        {
            "neighborhood_id": neighborhood_id,
            "neighborhood_name": name,
            "distance_to_center_km": distance,
            "school_score": school,
            "transit_score": transit,
            "safety_score": safety,
        }
        for neighborhood_id, name, distance, school, transit, safety, _ in NEIGHBORHOODS
    ]
    write_csv("neighborhoods.csv", NEIGHBORHOOD_COLUMNS, neighborhood_rows)
    write_csv("properties.csv", PROPERTY_COLUMNS, property_rows)
    write_csv("sales.csv", SALE_COLUMNS, sale_rows)
    counts = Counter(row["property_id"] for row in sale_rows)
    type_counts = Counter(row["property_type"] for row in property_rows)
    print(f"Seed: {SEED}")
    print(f"Properties: {len(property_rows):,}")
    print(f"Sales: {len(sale_rows):,}")
    print(f"Neighborhoods: {len(neighborhood_rows):,}")
    print(f"Properties with recorded sales: {len(counts):,}")
    print(f"Properties without sales: {len(property_rows)-len(counts):,}")
    print("Types: " + ", ".join(f"{k}: {v}" for k, v in sorted(type_counts.items())))
    print(f"CSV directory: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
