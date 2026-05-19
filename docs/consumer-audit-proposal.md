# Consumer Audit Proposal (Guide / Future Phase)

This document describes the **audit consumer**: a RabbitMQ listener that persists an **immutable historical snapshot** of each meal entry in **MongoDB**. It is **not implemented** yet.

Prerequisites:

- RabbitMQ fanout and `MealEntryCreated` contract — see [`rabbitmq-proposal.md`](rabbitmq-proposal.md)
- MongoDB running locally (Docker; to be added when this phase starts)

## Purpose

Answer: *“What did the user record at the moment they logged this meal?”*

This is **domain audit / activity history**, not infrastructure logging (stdout, Loki, etc.). If a dish or recipe is renamed or deleted in MySQL later, the audit document still reflects what was true when the meal was logged.

## What it does not do

- Does not update daily nutrition totals (see [`consumer-nutrition-proposal.md`](consumer-nutrition-proposal.md)).
- Does not replace MySQL for editing or deleting meal entries.
- Does not recalculate macros when catalog ingredients change.

## Position in the architecture

```text
exchange: meal-entry.events (fanout)
       |
       +----> queue: meal-entry.audit
                     |
                     v
              MealEntryAuditConsumer
                     |
                     v
              MongoDB: meal_entry_snapshots
```

The audit queue is bound to the **same** fanout exchange as the POC and nutrition queues. Each consumer receives a **copy** of every `MealEntryCreated` message.

## Input

Same JSON event as defined in [`rabbitmq-proposal.md`](rabbitmq-proposal.md#message-contract-mealentrycreated):

- `eventId`, `eventType`, `occurredAt`, `mealEntryId`, `eatenAt`
- `dishName`, `notes`
- `recipes[]` with resolved `components` and per-recipe `nutrition`
- `nutritionTotals` at meal level

The consumer should treat the message as the **source of truth** for the snapshot. It should not re-query MySQL or re-resolve recipes under normal operation.

## Processing steps

1. **Deserialize** `MealEntryCreated` from the queue.
2. **Validate** required fields: `eventId`, `mealEntryId`, `eventType == MEAL_ENTRY_CREATED`, non-empty `recipes` or explicit empty meal (edge case).
3. **Idempotency check**: query Mongo for an existing document with the same `eventId` (or unique index on `eventId`). If found → acknowledge and exit (safe on Rabbit redelivery).
4. **Insert** a new document in `meal_entry_snapshots` (append-only; no updates to prior snapshots for the same meal).
5. **Ack** the message.

On Mongo failure: do not ack (or nack with requeue) according to retry policy; after max retries, route to DLQ if configured.

## MongoDB collection: `meal_entry_snapshots`

One document per **event** (not per meal entry revision). Use `eventId` as the natural unique key.

### Proposed document shape

```json
{
  "_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "eventId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "eventType": "MEAL_ENTRY_CREATED",
  "mealEntryId": 42,
  "eatenAt": "2026-05-19T20:00:00",
  "recordedAt": "2026-05-19T20:30:15",
  "dishName": "Chicken bowl",
  "notes": "Post gym",
  "recipes": [
    {
      "recipeName": "Brown rice",
      "servingAmount": 1.0,
      "components": [
        {
          "ingredientName": "Brown rice",
          "quantity": 150.00,
          "unit": "g"
        }
      ],
      "nutrition": {
        "calories": 180.50,
        "proteins": 4.20,
        "carbohydrates": 38.00,
        "fats": 1.50
      }
    }
  ],
  "nutritionTotals": {
    "calories": 520.00,
    "proteins": 28.00,
    "carbohydrates": 55.00,
    "fats": 18.00
  }
}
```

Design choices:

| Choice | Rationale |
|--------|-----------|
| `_id` = `eventId` | Simple idempotency; one insert per event |
| Names over catalog IDs | Readable history even if catalog rows disappear |
| Store full `recipes` + `components` | Reconstruct “what I ate” without joins |
| Include `nutritionTotals` | UI and debugging without recomputation |
| `recordedAt` = `occurredAt` from event | Distinguish eat time vs system record time |

Optional metadata later: `schemaVersion`, `publisherHost`, `correlationId`.

### Indexes (proposal)

| Index | Use |
|-------|-----|
| Unique on `eventId` | Idempotency |
| `{ mealEntryId: 1, recordedAt: -1 }` | List history for one meal entry |
| `{ eatenAt: -1 }` | Recent meals feed |

## Application components (proposal)

| Component | Responsibility |
|-----------|----------------|
| `MealEntryAuditListener` | `@RabbitListener(queues = "meal-entry.audit")` |
| `MealEntrySnapshotDocument` | MongoDB document model |
| `MealEntrySnapshotRepository` | Spring Data Mongo `existsByEventId`, `save` |
| `RabbitMqConfig` | Declare `meal-entry.audit` queue + fanout binding |

Run the audit consumer **in the same Spring Boot app** as the API for simplicity, or split to a separate deployable later. Same repo, profile `audit-consumer` is enough for learning.

## Future API ideas (read from Mongo)

Not required for the first consumer version:

- `GET /meal-entries/recent` — last N snapshots by `eatenAt`
- `GET /meal-entries/{id}/history` — all snapshot events for `mealEntryId` (after delete events exist)

## Delete and update (later)

Today `MealEntryService.delete` removes rows in MySQL. Audit should eventually handle:

| Event | Audit behavior |
|-------|----------------|
| `MEAL_ENTRY_DELETED` | Insert new snapshot doc with `eventType` deleted + reference to `mealEntryId`; **do not** delete the original `MEAL_ENTRY_CREATED` snapshot |
| `MEAL_ENTRY_UPDATED` | New snapshot with full payload; optional link `supersedesEventId` |

Until those events exist, audit only covers **create**.

## Failure modes

| Scenario | Expected behavior |
|----------|-------------------|
| Duplicate delivery | Second insert blocked by unique `eventId`; ack |
| Partial Mongo outage | Retry; meal exists in MySQL but audit lags (eventual consistency) |
| Oversized payload | Unlikely for a single meal; monitor document size if nested recipes grow |

## Implementation checklist

- [ ] MongoDB Docker service + `spring-boot-starter-data-mongodb`
- [ ] Queue `meal-entry.audit` bound to `meal-entry.events`
- [ ] `MealEntryAuditListener` + repository
- [ ] Unique index on `eventId`
- [ ] Integration test: publish event → document in Mongo (Testcontainers or embedded flakiness avoided with test slice)

## Relationship to POC logger

The POC consumer on `meal-entry.poc` only logs. The audit consumer is the **first real side effect**: durable storage optimized for read-heavy history, separate from normalized MySQL.
