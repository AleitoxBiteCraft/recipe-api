# Dynamic Spicy Proposal (Guide / Domain Model)

This document proposes a future evolution for modeling spicy behavior as a derived property.
It is intentionally **not implemented** yet. Use it as a reference for future Flyway migrations and service logic.

## Motivation

- A recipe is not inherently spicy by tag alone.
- Spiciness emerges from ingredient choice, amount, and preparation.
- Manual `spicy` tags are useful for editorial curation, but not enough for accurate scoring.

## Goals

- Keep current data model stable while introducing dynamic spiciness.
- Compute recipe spiciness from ingredients instead of hardcoding labels.
- Allow richer behavior later (heat profiles, user tolerance, search ranking).

## Core Domain Idea

Spiciness should be derived from:

- Ingredient baseline heat (how spicy the ingredient is).
- Quantity used in the recipe.
- Preparation method impact (for example, raw vs boiled).

## Proposed Schema (Conceptual SQL)

### 1) Add baseline heat to ingredients

```sql
ALTER TABLE ingredient
    ADD COLUMN spicy_base DECIMAL(5,2) NOT NULL DEFAULT 0.00;
```

Notes:

- Suggested scale: `0.00` to `10.00`.
- `0.00` means non-spicy ingredient.

### 2) Add prep method and spice multiplier

```sql
CREATE TABLE prep_method (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    spicy_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_prep_method_name UNIQUE (name)
);
```

Examples:

- `raw` -> `1.00`
- `boiled` -> `0.70`
- `roasted` -> `0.90`

### 3) Link prep method to recipe-ingredient relation

Assuming table `recipe_ingredient` already exists:

```sql
ALTER TABLE recipe_ingredient
    ADD COLUMN prep_method_id INT NULL;

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT fk_recipe_ingredient_prep_method
        FOREIGN KEY (prep_method_id) REFERENCES prep_method(id);

CREATE INDEX idx_recipe_ingredient_prep_method_id
    ON recipe_ingredient(prep_method_id);
```

Optional hardening for quantity normalization:

```sql
ALTER TABLE recipe_ingredient
    ADD COLUMN quantity_normalized DECIMAL(10,4) NULL;
```

## Scoring Model

Per ingredient contribution:

```text
ingredient_spicy_score =
    ingredient.spicy_base
    * quantity_factor
    * COALESCE(prep_method.spicy_multiplier, 1.0)
```

Recipe score:

```text
recipe_spicy_score = SUM(ingredient_spicy_score)
```

Suggested levels (tunable):

- `0.0 - 1.0`: not spicy
- `1.0 - 3.0`: mild
- `3.0 - 6.0`: medium
- `6.0+`: hot

## API Strategy

- Expose `spicyScore` and `spicyLevel` in recipe responses.
- Keep editorial tags for curation, but do not rely on a static `spicy` tag as source of truth.
- Optionally allow filtering by ranges:
  - `minSpicyScore`
  - `maxSpicyScore`
  - `spicyLevel`

## MVP Implementation Plan

1. Add `ingredient.spicy_base`.
2. Add `prep_method` and optional relation from `recipe_ingredient`.
3. Seed default prep methods (`raw`, `boiled`, `roasted`).
4. Calculate spicy score in service layer (read-time).
5. Expose `spicyScore` in DTO/API.
6. Tune thresholds with sample data.

## Migration Strategy (When/If Implemented)

- Do not edit already applied migrations.
- Add forward-only Flyway migrations, for example:
  - `V2_1__add_ingredient_spicy_base.sql`
  - `V2_2__add_prep_method.sql`
  - `V2_3__link_recipe_ingredient_prep_method.sql`
  - `V2_4__seed_prep_methods.sql`
- Backfill baseline heat values incrementally.
- Keep import scripts idempotent where possible.

## Trade-Offs

- Pros:
  - More realistic than static tags.
  - Better sorting and filtering by actual heat.
  - Extensible to other dimensions (acidity, sweetness, bitterness).
- Cons:
  - Requires calibration of scales and thresholds.
  - Quantity normalization can become complex if units vary heavily.

## Recommendation

Implement the MVP first and keep the score as a derived runtime value.
Only persist precomputed recipe scores if performance or query complexity requires it later.
