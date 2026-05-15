# Database schema (current)

Entity-relationship view of the **implemented** schema. Source of truth: Flyway migration [`V1_0__create_tables.sql`](../src/main/resources/db/migration/V1_0__create_tables.sql).

For planned changes, see [`schema-evolution-proposal.md`](schema-evolution-proposal.md).

## ER diagram

```mermaid
erDiagram
    ingredient {
        int id PK
        varchar name UK "NOT NULL"
        decimal calories_per_100g "CHECK >= 0"
        decimal proteins_per_100g "CHECK >= 0"
        decimal carbohydrates_per_100g "CHECK >= 0"
        decimal fats_per_100g "CHECK >= 0"
        varchar nutrition_source "NULL"
        datetime created_at
        datetime updated_at
    }

    recipe {
        int id PK
        varchar name UK "NOT NULL"
        text description
        int serving "NULL, CHECK > 0 when set"
        datetime created_at
        datetime updated_at
    }

    tag {
        int id PK
        varchar name UK "NOT NULL"
        datetime created_at
        datetime updated_at
    }

    recipe_tag {
        int id PK
        int recipe_id FK
        int tag_id FK
        datetime created_at
        datetime updated_at
    }

    recipe_component {
        int id PK
        int recipe_id FK
        varchar component_type "INGREDIENT | RECIPE"
        int ingredient_id FK "NULL"
        int child_recipe_id FK "NULL, self-ref"
        decimal quantity "CHECK > 0"
        varchar unit "DEFAULT g"
        datetime created_at
        datetime updated_at
    }

    recipe_step {
        int id PK
        int recipe_id FK
        int step_order "UK per recipe, CHECK > 0"
        text description
        datetime created_at
        datetime updated_at
    }

    dish {
        int id PK
        varchar name UK "NOT NULL"
        text description
        datetime created_at
        datetime updated_at
    }

    dish_recipe {
        int id PK
        int dish_id FK
        int recipe_id FK
        datetime created_at
        datetime updated_at
    }

    meal_entry {
        int id PK
        int dish_id FK "NULL"
        datetime eaten_at
        text notes
        datetime created_at
        datetime updated_at
    }

    meal_entry_recipe {
        int id PK
        int meal_entry_id FK
        int recipe_id FK
        decimal quantity "CHECK > 0"
        varchar unit "DEFAULT g"
        datetime created_at
        datetime updated_at
    }

    recipe ||--o{ recipe_tag : "tiene"
    tag ||--o{ recipe_tag : "clasifica"

    recipe ||--o{ recipe_component : "compone"
    ingredient ||--o{ recipe_component : "como ingrediente"
    recipe ||--o{ recipe_component : "como sub-receta"

    recipe ||--o{ recipe_step : "pasos"

    dish ||--o{ dish_recipe : "incluye"
    recipe ||--o{ dish_recipe : "forma parte de"

    dish ||--o| meal_entry : "opcional"
    meal_entry ||--o{ meal_entry_recipe : "porciones"
    recipe ||--o{ meal_entry_recipe : "consumida en"
```

## Tables by domain

| Domain | Tables | Role |
|--------|--------|------|
| Catalog | `ingredient`, `recipe`, `tag`, `dish` | Core entities; `recipe.serving` optional yield in servings |
| Recipe detail | `recipe_tag`, `recipe_component`, `recipe_step` | Tags, composition (ingredient or sub-recipe), steps |
| Dish | `dish_recipe` | Many-to-many: dish ↔ recipes |
| Meal log | `meal_entry`, `meal_entry_recipe` | Logged meal; optional dish; recipes with quantity |

## Business rules (constraints)

- **`recipe`**: `serving` is nullable (e.g. sub-recipes like sauces). When set, must be `> 0` (`chk_recipe_serving`).
- **`recipe_component`**: `component_type` is `INGREDIENT` or `RECIPE`. Exactly one of `ingredient_id` or `child_recipe_id` must be set, matching the type. A recipe cannot reference itself as `child_recipe_id`.
- **Uniqueness**: `(recipe_id, tag_id)`, `(dish_id, recipe_id)`, `(recipe_id, step_order)`; component rows are unique per recipe and reference (ingredient or child recipe).
- **`meal_entry`**: `dish_id` is optional; linked recipes live in `meal_entry_recipe`.

## Maintenance

When the schema changes in a new Flyway migration, update this diagram or add a short note here pointing to the migration that superseded part of the model.
