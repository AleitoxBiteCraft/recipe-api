# Consumer Nutrition Proposal (Guide / Future Phase)

This document describes the **nutrition consumer**: a RabbitMQ listener that maintains **daily macro rollups** in **MongoDB** when meals are logged. It is **not implemented** yet.

Prerequisites:

- RabbitMQ fanout and `MealEntryCreated` contract — see [`rabbitmq-proposal.md`](rabbitmq-proposal.md)
- MongoDB (same local Docker stack as audit; can share one `docker-compose` file)

Related:

- [`consumer-audit-proposal.md`](consumer-audit-proposal.md) — immutable per-meal snapshots (different question, different collection)

## Purpose

Answer: *“How much have I eaten today (calories and macros)?”*

The consumer **aggregates** pre-calculated numbers from the event. It does not rebuild nutrition from the MySQL catalog at listen time.

## What it does not do

- Does not store the full meal narrative (audit doc does).
- Does not replace `GET /meal-entries/{id}/resolved` for meal detail.
- Does not retroactively change past days when ingredient `caloriesPer100g` is updated in MySQL (product decision: rollups are **point-in-time** via event payloads).

## Position in the architecture

```text
exchange: meal-entry.events (fanout)
       |
       +----> queue: meal-entry.nutrition
                     |
                     v
              MealEntryNutritionConsumer
                     |
                     v
              MongoDB: daily_nutrition
```

Same fanout message as audit and POC; different queue and different Mongo update semantics.

## Input

Uses `nutritionTotals` and `eatenAt` from `MealEntryCreated` (see [`rabbitmq-proposal.md`](rabbitmq-proposal.md#message-contract-mealentrycreated)).

| Field | Usage |
|-------|--------|
| `eventId` | Idempotency — must not apply the same meal twice |
| `mealEntryId` | Reference in daily `meals[]` sub-document |
| `eatenAt` | **Calendar day key** for the rollup (`eatenAt.toLocalDate()`, not `occurredAt`) |
| `nutritionTotals` | Increment `calories`, `proteins`, `carbohydrates`, `fats` |

The consumer should **not** sum `recipes[].nutrition` again unless validating publisher math in dev; trust `nutritionTotals` for production path.

## Processing steps

1. **Deserialize** `MealEntryCreated`.
2. **Validate** `eventId`, `eatenAt`, `nutritionTotals` present and non-null.
3. **Derive day key**: `date = eatenAt` converted to local date (document timezone policy: UTC or system default — pick one and document in config).
4. **Idempotency**: if `processed_events` (embedded or separate collection) already contains `eventId` for this consumer → ack and exit.
5. **Upsert** `daily_nutrition` for `date`:
   - `$inc` each field in `totals`
   - `$inc mealCount` by 1
   - `$push` to `meals` a small reference: `{ mealEntryId, eventId, eatenAt, nutritionTotals }`
   - Record `eventId` in processed set (same transaction if using Mongo multi-doc ACID in a session, or unique index on `nutrition_processed_events.eventId`)
6. **Ack** the message.

## MongoDB collection: `daily_nutrition`

One document per **calendar day** (by `eatenAt` date).

### Proposed document shape

```json
{
  "_id": "2026-05-19",
  "date": "2026-05-19",
  "totals": {
    "calories": 1840.50,
    "proteins": 95.20,
    "carbohydrates": 210.00,
    "fats": 62.30
  },
  "mealCount": 4,
  "meals": [
    {
      "mealEntryId": 42,
      "eventId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "eatenAt": "2026-05-19T20:00:00",
      "nutrition": {
        "calories": 520.00,
        "proteins": 28.00,
        "carbohydrates": 55.00,
        "fats": 18.00
      }
    }
  ],
  "updatedAt": "2026-05-19T22:15:00"
}
```

| Field | Notes |
|-------|--------|
| `_id` / `date` | ISO date string `YYYY-MM-DD` from `eatenAt` |
| `totals` | Running sum for the day |
| `mealCount` | Number of counted meals (increment per successful event) |
| `meals[]` | Optional detail for drill-down UI; can cap size later |
| `updatedAt` | Last aggregation write |

### Idempotency options

| Approach | Pros | Cons |
|----------|------|------|
| Unique index on `nutrition_processed_events.eventId` | Clear audit trail of processed IDs | Extra collection |
| Check `meals.eventId` before `$inc` | Single collection | Array scan unless indexed |
| Redis SET `processed:{eventId}` | Fast | Another dependency |

Recommendation: **`nutrition_processed_events`** with `{ eventId: 1 }` unique index, or guard upsert with `meals.eventId` + partial unique index if Mongo version supports it.

**Critical:** duplicate processing **inflates** daily totals. Nutrition consumer idempotency is stricter than audit.

## Application components (proposal)

| Component | Responsibility |
|-----------|----------------|
| `MealEntryNutritionListener` | `@RabbitListener(queues = "meal-entry.nutrition")` |
| `DailyNutritionDocument` | Mongo model for `daily_nutrition` |
| `DailyNutritionRepository` | Custom update with `$inc` / upsert |
| `ProcessedEventRepository` | Optional idempotency store |

## Future API ideas (read from Mongo)

- `GET /nutrition/daily?date=2026-05-19` → `daily_nutrition` document
- `GET /nutrition/daily/today` → convenience wrapper

MySQL remains authoritative for “does meal entry 42 exist?”; Mongo answers “what were that day’s totals when events were processed?”

## Delete and update (later)

When `MealEntryService.delete` publishes `MealEntryDeleted`:

| Step | Action |
|------|--------|
| 1 | Load original `MEAL_ENTRY_CREATED` `eventId` or subtract using payload on delete event (include same `nutritionTotals` on delete message) |
| 2 | `$inc` totals by **negative** amounts |
| 3 | `$pull` meal from `meals[]` by `mealEntryId` or `eventId` |
| 4 | Record delete `eventId` in processed set |

Without delete events, daily totals **over-count** after MySQL deletes.

## Edge cases

| Scenario | Behavior |
|----------|----------|
| Meal logged at 23:50, `eatenAt` yesterday | Rollup goes to **yesterday’s** document |
| Meal spans midnight in UI | User’s `eatenAt` wins; not `occurredAt` |
| Zero-calorie meal | Still increment `mealCount`; totals unchanged |
| Publisher bug (null totals) | Reject / DLQ; do not write partial corrupt state |

## Comparison with audit consumer

| | Audit | Nutrition |
|---|--------|-----------|
| Operation | `insert` snapshot | `upsert` + `$inc` |
| Collection | `meal_entry_snapshots` | `daily_nutrition` |
| Idempotency | Duplicate = duplicate doc (bad) | Duplicate = inflated totals (worse) |
| Primary key | `eventId` | `date` (+ `eventId` for dedup) |
| Question answered | What was logged? | How much today? |

## Publisher responsibility

Nutrition consumer is intentionally **dumb**. The API publisher must:

1. Resolve meal composition (adjustments applied).
2. Compute macros using the same rules as `RecipeService` (per-100g scaling, nested recipe batches, units `g` / `ml` / batch).
3. Put correct `nutritionTotals` on `MealEntryCreated`.

Extracting `MealEntryNutritionCalculator` (or similar) shared between REST “preview” and publisher avoids drift.

## Implementation checklist

- [ ] Queue `meal-entry.nutrition` on fanout exchange
- [ ] `MealEntryNutritionListener` + `DailyNutritionRepository` upsert
- [ ] Idempotency store for `eventId`
- [ ] Timezone policy in `application.properties` (`app.nutrition.day-zone=UTC`)
- [ ] Test: two events same day → summed totals; same `eventId` twice → unchanged totals
- [ ] (Later) `MealEntryDeleted` consumer logic

## POC vs this consumer

The RabbitMQ POC logs messages on `meal-entry.poc`. The nutrition consumer is a **later phase** after Mongo is available. It can be developed in parallel with audit (same message, different Mongo writes) once fanout has both queues bound.
