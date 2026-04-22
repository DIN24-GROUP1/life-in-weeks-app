#!/usr/bin/env python3
"""
Generate a synthetic dataset of 1000 Memento users for data analysis.

Output files (written next to this script):
    users_dataset.json   — full structured dataset
    users_dataset.csv    — flat table ready for Excel / pandas / R

Usage:
    python scripts/seed_firestore.py

No external dependencies required — only the Python standard library.
"""

import csv
import json
import math
import random
import string
from collections import Counter
from datetime import date, timedelta

# ── Constants ─────────────────────────────────────────────────────────────────

TODAY   = date(2026, 4, 22)
SEED    = 42
N_USERS = 1000

# ── Helpers ───────────────────────────────────────────────────────────────────

def add_years(d: date, years: int) -> date:
    try:
        return d.replace(year=d.year + years)
    except ValueError:       # Feb 29 on a non-leap year
        return d.replace(year=d.year + years, day=28)

def fake_uid(length: int = 28) -> str:
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

# ── Countries (name, maleLE, femaleLE, population_weight, region) ─────────────
# Weights are a simplified proportional approximation of world population.
# Life expectancy values match CountryData.kt (WHO 2021).

COUNTRIES = [
    # name                       mLE  fLE  wt   region
    ("Afghanistan",               63,  65,   8, "Asia"),
    ("Albania",                   76,  80,   1, "Europe"),
    ("Algeria",                   77,  78,   3, "Africa"),
    ("Angola",                    63,  68,   2, "Africa"),
    ("Argentina",                 73,  79,   4, "South America"),
    ("Armenia",                   71,  78,   1, "Asia"),
    ("Australia",                 81,  85,   4, "Oceania"),
    ("Austria",                   79,  84,   2, "Europe"),
    ("Azerbaijan",                71,  77,   1, "Asia"),
    ("Bangladesh",                72,  75,  12, "Asia"),
    ("Belgium",                   79,  84,   2, "Europe"),
    ("Bolivia",                   70,  76,   1, "South America"),
    ("Bosnia and Herzegovina",    74,  79,   1, "Europe"),
    ("Brazil",                    73,  80,  14, "South America"),
    ("Bulgaria",                  71,  79,   1, "Europe"),
    ("Cameroon",                  60,  63,   2, "Africa"),
    ("Canada",                    80,  84,   5, "North America"),
    ("Chile",                     78,  83,   2, "South America"),
    ("China",                     75,  80,  25, "Asia"),
    ("Colombia",                  74,  80,   4, "South America"),
    ("Czech Republic",            76,  82,   2, "Europe"),
    ("Denmark",                   79,  83,   1, "Europe"),
    ("DR Congo",                  59,  63,   6, "Africa"),
    ("Ecuador",                   75,  79,   1, "South America"),
    ("Egypt",                     72,  75,   8, "Africa"),
    ("Ethiopia",                  66,  70,   9, "Africa"),
    ("Finland",                   79,  84,   1, "Europe"),
    ("France",                    79,  85,   5, "Europe"),
    ("Germany",                   79,  83,   6, "Europe"),
    ("Ghana",                     63,  67,   2, "Africa"),
    ("Greece",                    79,  84,   1, "Europe"),
    ("Guatemala",                 72,  78,   1, "North America"),
    ("Hungary",                   72,  79,   1, "Europe"),
    ("India",                     68,  71,  30, "Asia"),
    ("Indonesia",                 69,  73,  18, "Asia"),
    ("Iran",                      75,  78,   6, "Asia"),
    ("Iraq",                      70,  74,   4, "Asia"),
    ("Ireland",                   80,  84,   1, "Europe"),
    ("Israel",                    81,  84,   1, "Asia"),
    ("Italy",                     81,  85,   4, "Europe"),
    ("Japan",                     81,  87,   8, "Asia"),
    ("Jordan",                    74,  77,   1, "Asia"),
    ("Kazakhstan",                67,  76,   2, "Asia"),
    ("Kenya",                     65,  70,   4, "Africa"),
    ("Malaysia",                  74,  78,   3, "Asia"),
    ("Mexico",                    73,  78,  10, "North America"),
    ("Morocco",                   74,  78,   3, "Africa"),
    ("Mozambique",                58,  63,   2, "Africa"),
    ("Myanmar",                   65,  70,   4, "Asia"),
    ("Nepal",                     70,  73,   3, "Asia"),
    ("Netherlands",               80,  83,   2, "Europe"),
    ("Nigeria",                   55,  57,  15, "Africa"),
    ("Norway",                    81,  84,   1, "Europe"),
    ("Pakistan",                  67,  69,  18, "Asia"),
    ("Peru",                      74,  79,   3, "South America"),
    ("Philippines",               68,  75,   9, "Asia"),
    ("Poland",                    73,  81,   3, "Europe"),
    ("Portugal",                  78,  84,   1, "Europe"),
    ("Romania",                   71,  79,   2, "Europe"),
    ("Russia",                    66,  76,  10, "Europe"),
    ("Saudi Arabia",              77,  80,   3, "Asia"),
    ("Serbia",                    73,  78,   1, "Europe"),
    ("Singapore",                 81,  86,   1, "Asia"),
    ("South Africa",              60,  67,   5, "Africa"),
    ("South Korea",               80,  86,   4, "Asia"),
    ("Spain",                     80,  86,   4, "Europe"),
    ("Sri Lanka",                 73,  79,   2, "Asia"),
    ("Sudan",                     65,  68,   3, "Africa"),
    ("Sweden",                    81,  84,   1, "Europe"),
    ("Switzerland",               81,  85,   1, "Europe"),
    ("Tanzania",                  65,  68,   4, "Africa"),
    ("Thailand",                  73,  80,   5, "Asia"),
    ("Turkey",                    76,  81,   7, "Asia"),
    ("Uganda",                    63,  67,   3, "Africa"),
    ("Ukraine",                   66,  76,   3, "Europe"),
    ("United Kingdom",            79,  83,   5, "Europe"),
    ("United States",             74,  80,  20, "North America"),
    ("Uzbekistan",                71,  76,   3, "Asia"),
    ("Venezuela",                 70,  77,   2, "South America"),
    ("Vietnam",                   71,  79,   8, "Asia"),
    ("Yemen",                     64,  68,   2, "Asia"),
    ("Zambia",                    62,  67,   1, "Africa"),
    ("Zimbabwe",                  60,  65,   1, "Africa"),
]

_C_NAMES   = [c[0] for c in COUNTRIES]
_C_MAP     = {c[0]: c for c in COUNTRIES}
_C_WEIGHTS = [c[3] for c in COUNTRIES]

# ── Gender slider ──────────────────────────────────────────────────────────────
# 0 = Male, 9 = Female (10 discrete positions).
# Real-world approximation: ~45 % pick 0, ~45 % pick 9,
# ~10 % pick somewhere in between (bell-shaped around 4–5).

_SLIDER_VALUES  = list(range(10))
_SLIDER_WEIGHTS = [45, 1, 1, 2, 3, 3, 2, 1, 1, 45]   # ~9.6 % in-between

# ── Phase templates ────────────────────────────────────────────────────────────
# (name, start_year, end_year, template_label)

TEMPLATES = [
    ("App defaults",      [("Childhood",0,6),("School",6,18),("University",18,23),("Career",23,65),("Senior",65,90)]),
    ("No university",     [("Childhood",0,6),("School",6,18),("Career",18,65),("Senior",65,90)]),
    ("Minimalist",        [("Growing Up",0,25),("Adult Life",25,85)]),
    ("Simple 3-phase",    [("Youth",0,18),("Adulthood",18,65),("Senior",65,90)]),
    ("Family-focused",    [("Childhood",0,6),("School",6,18),("Young Adult",18,30),("Family Years",30,55),("Empty Nest",55,70),("Senior",70,90)]),
    ("Academic / PhD",    [("Childhood",0,6),("School",6,18),("Bachelor's",18,21),("Master's",21,23),("PhD",23,28),("Academic Career",28,65),("Retirement",65,90)]),
    ("Entrepreneur",      [("Childhood",0,6),("School",6,18),("Startup Years",18,30),("Growth Phase",30,55),("Legacy",55,85)]),
    ("Early retirement",  [("Childhood",0,6),("Education",6,22),("Career",22,45),("Early Retirement",45,70),("Senior",70,90)]),
    ("Creative / Artist", [("Childhood",0,6),("Education",6,20),("Emerging",20,35),("Established",35,60),("Legacy",60,85)]),
    ("Military",          [("Childhood",0,6),("School",6,18),("Military Service",18,38),("Civilian Career",38,62),("Retirement",62,85)]),
    ("Healthcare",        [("Childhood",0,6),("School",6,18),("University",18,24),("Residency",24,30),("Career",30,65),("Retirement",65,90)]),
    ("Adventurer",        [("Childhood",0,6),("School",6,18),("Exploration",18,25),("Settled Career",25,60),("Senior",60,90)]),
]

_T_WEIGHTS = [28, 12, 4, 8, 9, 5, 7, 6, 4, 3, 7, 7]

# ── Generator ──────────────────────────────────────────────────────────────────

def _birthday_for_age(age: int) -> date:
    year  = TODAY.year - age
    month = random.randint(1, 12)
    max_d = 28 if month == 2 else 30 if month in (4, 6, 9, 11) else 31
    d     = date(year, month, random.randint(1, max_d))
    return d.replace(year=year - 1) if d > TODAY else d

def _life_expectancy(male_le: int, female_le: int, slider: int) -> int:
    return int(male_le * (1 - slider / 9) + female_le * (slider / 9))

def generate_user() -> dict:
    country_name               = random.choices(_C_NAMES, weights=_C_WEIGHTS, k=1)[0]
    _, male_le, female_le, _, region = _C_MAP[country_name]

    slider          = random.choices(_SLIDER_VALUES, weights=_SLIDER_WEIGHTS, k=1)[0]
    life_expectancy = _life_expectancy(male_le, female_le, slider)

    # Cap age at life expectancy so no user outlives their expected lifespan
    age      = random.randint(16, min(92, life_expectancy))
    birthday = _birthday_for_age(age)

    template_label, phases_def = random.choices(TEMPLATES, weights=_T_WEIGHTS, k=1)[0]

    weeks_lived = max(0, (TODAY - birthday).days // 7)
    weeks_total = life_expectancy * 52

    phases = []
    for idx, (name, start_y, end_y) in enumerate(phases_def, 1):
        phases.append({
            "id":        idx,
            "name":      name,
            "startYear": start_y,
            "endYear":   end_y,
        })

    return {
        "uid":                   fake_uid(),
        "age":                   age,
        "birthday":              birthday.strftime("%d.%m.%Y"),
        "country":               country_name,
        "region":                region,
        "genderSlider":          slider,
        "genderLabel":           "Male" if slider == 0 else "Female" if slider == 9 else "Non-binary / Other",
        "lifeExpectancy":        life_expectancy,
        "weeksLived":            weeks_lived,
        "weeksRemaining":        max(0, weeks_total - weeks_lived),
        "weeksTotal":            weeks_total,
        "percentLived":          round(weeks_lived / weeks_total * 100, 1),
        "phaseTemplate":         template_label,
        "phaseCount":            len(phases),
        "phases":                phases,
    }

# ── Output ─────────────────────────────────────────────────────────────────────

def save_json(users: list, path: str) -> None:
    with open(path, "w", encoding="utf-8") as f:
        json.dump(users, f, indent=2, ensure_ascii=False)
    print(f"JSON saved  -> {path}")

def save_csv(users: list, path: str) -> None:
    """Flat CSV — one row per user, phases serialised as a semicolon-separated string."""
    fieldnames = [
        "uid", "age", "birthday", "country", "region",
        "genderSlider", "genderLabel", "lifeExpectancy",
        "weeksLived", "weeksRemaining", "weeksTotal", "percentLived",
        "phaseTemplate", "phaseCount", "phases",
    ]
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for u in users:
            row = {k: u[k] for k in fieldnames if k != "phases"}
            row["phases"] = "; ".join(
                f"{p['name']} ({p['startYear']}–{p['endYear']})" for p in u["phases"]
            )
            writer.writerow(row)
    print(f"CSV saved   -> {path}")

# ── Statistics ─────────────────────────────────────────────────────────────────

def print_stats(users: list) -> None:
    print("\n--- Gender slider distribution ---")
    sliders = [u["genderSlider"] for u in users]
    for v in range(10):
        count = sliders.count(v)
        label = "(Male)  " if v == 0 else "(Female)" if v == 9 else "        "
        bar   = "#" * (count // 5)
        print(f"  {v} {label}  {count:4d}  {bar}")

    print("\n--- Top 15 countries ---")
    for country, count in Counter(u["country"] for u in users).most_common(15):
        print(f"  {country:<32}  {count:4d}  {'#' * (count // 3)}")

    print("\n--- Users by region ---")
    for region, count in sorted(Counter(u["region"] for u in users).items(),
                                key=lambda x: -x[1]):
        print(f"  {region:<20}  {count:4d}  {'#' * (count // 10)}")

    print("\n--- Phase template distribution ---")
    for tmpl, count in Counter(u["phaseTemplate"] for u in users).most_common():
        print(f"  {tmpl:<22}  {count:4d}  {'#' * (count // 5)}")

    ages = [u["age"] for u in users]
    les  = [u["lifeExpectancy"] for u in users]
    pcts = [u["percentLived"] for u in users]
    print(f"\n--- Age stats ---  min {min(ages)}  max {max(ages)}  avg {sum(ages)/len(ages):.1f}")
    print(f"--- Life exp.  ---  min {min(les)}  max {max(les)}  avg {sum(les)/len(les):.1f}")
    print(f"--- % lived    ---  min {min(pcts):.1f}  max {max(pcts):.1f}  avg {sum(pcts)/len(pcts):.1f}")
    print(f"\n--- Countries represented: {len(Counter(u['country'] for u in users))} ---\n")

# ── Main ───────────────────────────────────────────────────────────────────────

def main() -> None:
    import os
    random.seed(SEED)

    print(f"Generating {N_USERS} users  (seed={SEED})...")
    users = [generate_user() for _ in range(N_USERS)]

    base = os.path.dirname(os.path.abspath(__file__))
    save_json(users, os.path.join(base, "users_dataset.json"))
    save_csv (users, os.path.join(base, "users_dataset.csv"))

    print_stats(users)


if __name__ == "__main__":
    main()
