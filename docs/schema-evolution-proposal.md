# Schema Evolution Proposal (Guide / Technical Debt)

This document proposes **future** schema evolution not yet in Flyway.

**Implemented in `V1_0__create_tables.sql`:** full meal log (`meal_entry`, `meal_entry_recipe`, `meal_entry_recipe_adjustment`). Behavior and use cases are documented in [`database-schema.md`](database-schema.md#meal-log--use-cases).

## Goals

- Keep current model stable while loading recipes.
- Avoid duplicate records (recipes and relations).
- Add metadata for discoverability and procurement.

## Meal log — possible follow-ups (not implemented)

| Idea | Notes |
|------|--------|
| `MODIFY` adjustment | Change `quantity`/`unit` of an existing `recipe_component` in one row instead of REMOVE + ADD |
| Catalog alternatives | Optional ingredient groups on `recipe_component` (e.g. milk \| cream) reused across recipes |
| Promote variant to `dish` | UX flow: save a frequent meal composition as a new dish template — no extra tables required initially |

## 1) Cooking Courses (Recipe Origin)

```sql
CREATE TABLE course (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(255) NULL,
    edition VARCHAR(100) NULL,
    started_at DATE NULL,
    ended_at DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_course_name_provider_edition
        UNIQUE (name, provider, edition)
);

CREATE TABLE recipe_course (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    course_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipe_course_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT fk_recipe_course_course
        FOREIGN KEY (course_id) REFERENCES course(id),

    CONSTRAINT uq_recipe_course
        UNIQUE (recipe_id, course_id)
);

CREATE INDEX idx_recipe_course_recipe_id
    ON recipe_course(recipe_id);

CREATE INDEX idx_recipe_course_course_id
    ON recipe_course(course_id);
```

## 2) Ingredient Sources (Where to Buy)

```sql
CREATE TABLE supplier (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    supplier_type VARCHAR(50) NULL,
    website_url VARCHAR(500) NULL,
    location_hint VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_supplier_name_location
        UNIQUE (name, location_hint)
);

CREATE TABLE ingredient_supplier (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id INT NOT NULL,
    supplier_id INT NOT NULL,
    product_name VARCHAR(255) NULL,
    product_url VARCHAR(500) NULL,
    notes TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ingredient_supplier_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),

    CONSTRAINT fk_ingredient_supplier_supplier
        FOREIGN KEY (supplier_id) REFERENCES supplier(id),

    CONSTRAINT uq_ingredient_supplier
        UNIQUE (ingredient_id, supplier_id)
);

CREATE INDEX idx_ingredient_supplier_ingredient_id
    ON ingredient_supplier(ingredient_id);

CREATE INDEX idx_ingredient_supplier_supplier_id
    ON ingredient_supplier(supplier_id);
```

## 3) Required Tools by Recipe

```sql
CREATE TABLE tool (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_tool_name UNIQUE (name)
);

CREATE TABLE recipe_tool (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    tool_id INT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipe_tool_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT fk_recipe_tool_tool
        FOREIGN KEY (tool_id) REFERENCES tool(id),

    CONSTRAINT uq_recipe_tool
        UNIQUE (recipe_id, tool_id)
);

CREATE INDEX idx_recipe_tool_recipe_id
    ON recipe_tool(recipe_id);

CREATE INDEX idx_recipe_tool_tool_id
    ON recipe_tool(tool_id);
```

## 4) Optional Hardening for Recipe Deduplication

If recipe imports come from external sources, consider adding:

```sql
ALTER TABLE recipe
    ADD COLUMN source VARCHAR(100) NULL,
    ADD COLUMN source_external_id VARCHAR(255) NULL,
    ADD COLUMN slug VARCHAR(255) NULL;

ALTER TABLE recipe
    ADD CONSTRAINT uq_recipe_source_external_id
        UNIQUE (source, source_external_id);

ALTER TABLE recipe
    ADD CONSTRAINT uq_recipe_slug
        UNIQUE (slug);
```

## Migration Strategy (When/If Implemented)

- Do not edit `V1_0__create_tables.sql` after being applied.
- Add forward-only migrations, for example:
  - `V1_1__add_course_and_recipe_course.sql`
  - `V1_2__add_supplier_and_ingredient_supplier.sql`
  - `V1_3__add_tool_and_recipe_tool.sql`
  - `V1_4__add_recipe_dedup_keys.sql` (optional)
- Backfill data incrementally and keep import scripts idempotent where possible.
