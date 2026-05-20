# RabbitMQ Integration Proposal (Guide / POC)

This document describes **RabbitMQ** integration in `recipe-api` for asynchronous side effects after meal log writes. **Phase 1 (POC)** is implemented; phase 2+ items below remain future work.

Related follow-ups (separate docs, later phases):

- [`consumer-audit-proposal.md`](consumer-audit-proposal.md) — immutable meal snapshots in MongoDB
- [`consumer-nutrition-proposal.md`](consumer-nutrition-proposal.md) — daily macro rollups in MongoDB

## Goals

- Decouple **meal entry creation** in MySQL from downstream processing (audit, nutrition, future consumers).
- Learn RabbitMQ with a **small, realistic** flow tied to the existing meal log domain.
- Keep **MySQL as the source of truth** for CRUD; messages carry a **denormalized snapshot** for consumers.
- Start with a **POC**: fanout exchange, one queue, one consumer that only logs the message at `INFO`.

## Non-goals (POC phase)

- MongoDB consumers (see linked proposals).
- Outbox table / transactional outbox (acceptable technical debt for POC; document as phase 2 hardening).
- Publishing on `DELETE` or `UPDATE` (future events; see consumer proposals).
- Running RabbitMQ in production deployment (local Docker only for now).

## Trigger

Publish **after** a successful `MealEntryService.create(...)` transaction commit.

Do **not** publish if validation fails or the transaction rolls back. Prefer **after-commit** publishing (`@TransactionalEventListener(phase = AFTER_COMMIT)` or equivalent) so consumers never see events for rolled-back rows.

## Local infrastructure: Docker

RabbitMQ should run in Docker during local development so the broker lifecycle is independent of the Spring app and easy to reset.

### Proposed `docker-compose.yml` (repo root)

Add a minimal service (management UI optional but useful for debugging bindings and queues):

```yaml
services:
  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: recipe-api-rabbitmq
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI (guest/guest)
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  rabbitmq_data:
```

**Usage**

```bash
docker compose up -d rabbitmq
```

- AMQP: `localhost:5672`
- Management UI: http://localhost:15672 (user `guest`, password `guest`)

MySQL can remain as today (local install or a separate compose service later). RabbitMQ does not replace MySQL.

### Spring configuration (proposal)

`application.properties` (local):

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

`application-test.properties`:

- Either disable Rabbit auto-configuration for integration tests, or use Testcontainers / embedded listener mocks.
- POC recommendation: **disable** publishing in tests (`spring.rabbitmq.listener.simple.auto-startup=false` and a no-op publisher bean profile) until dedicated messaging tests exist.

## Dependencies

Add to `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```

Spring AMQP provides `RabbitTemplate`, listener containers, and JSON message conversion.

## Topology (fanout)

```text
[recipe-api]  MealEntryService.create()
       |
       |  (after commit)
       v
  MealEntryEventPublisher
       |
       v
  exchange: meal-entry.events  (type: fanout, durable)
       |
       +---- binding ----> queue: meal-entry.poc
       |                         |
       |                         v
       |                  MealEntryPocConsumer (INFO log)
       |
       +---- (future) ----> queue: meal-entry.audit
       |
       +---- (future) ----> queue: meal-entry.nutrition
```

| Artifact | Name | Notes |
|----------|------|--------|
| Exchange | `meal-entry.events` | `FanoutExchange`, durable |
| Queue (POC) | `meal-entry.poc` | Durable, bound to exchange |
| Routing key | *(none)* | Fanout ignores routing keys |

Declare exchange, queue, and binding in a `@Configuration` class (or via `RabbitAdmin` beans) so the app is self-contained on startup.

## Message contract: `MealEntryCreated`

Single event type for the POC. JSON body, `contentType: application/json`.

### Envelope fields

| Field | Type | Description |
|-------|------|-------------|
| `eventId` | UUID string | Unique per publish; idempotency key for future consumers |
| `eventType` | string | `MEAL_ENTRY_CREATED` |
| `occurredAt` | ISO-8601 datetime | When the event was published (system clock) |
| `mealEntryId` | integer | MySQL `meal_entry.id` |
| `eatenAt` | ISO-8601 datetime | When the meal was eaten (`meal_entry.eaten_at`) |
| `dishName` | string | Denormalized dish name at publish time |
| `notes` | string, nullable | Meal notes |
| `recipes` | array | Resolved recipes with components and per-recipe nutrition |
| `nutritionTotals` | object | Meal-level totals (sum of recipes) |

### `recipes[]` (aligned with resolved meal model)

Each item mirrors the resolved meal shape used by `GET .../resolved` today:

| Field | Description |
|-------|-------------|
| `recipeName` | Recipe name at publish time |
| `servingAmount` | Portion eaten |
| `components` | Resolved components after adjustments (ingredient name, quantity, unit) |
| `nutrition` | Per-recipe totals: `calories`, `proteins`, `carbohydrates`, `fats` |

Catalog IDs (`dishId`, `recipeId`) are **optional** in the POC payload. Prefer human-readable snapshot fields for consumers; include `mealEntryId` for correlation with MySQL.

### `nutritionTotals`

Same four macros as `RecipeResponseDto` (`totalCalories`, etc.), rounded consistently with `RecipeService` (2 decimal places, half-up).

### Example payload

```json
{
  "eventId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "eventType": "MEAL_ENTRY_CREATED",
  "occurredAt": "2026-05-19T20:30:15",
  "mealEntryId": 42,
  "eatenAt": "2026-05-19T20:00:00",
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

### Building the payload (publisher responsibility)

After `create` persists rows:

1. Load the meal entry in **resolved** form (same data as `MealEntryService.getResolvedById`).
2. Compute per-recipe and meal-level macros using ingredient `*Per100g` fields and the same rules as `RecipeService` (reuse or extract shared nutrition logic; meal-entry adjustments must affect totals).
3. Map to `MealEntryCreatedEvent` (Java record) and publish via `RabbitTemplate`.

Nutrition is **not** stored in MySQL on `meal_entry` today; the event is the first place that ships **calculated** macros for the actual eaten composition.

## Application structure (proposal)

| Component | Responsibility |
|-----------|----------------|
| `MealEntryCreatedEvent` | Immutable message DTO (package e.g. `...messaging.event`) |
| `MealEntryEventPublisher` | Converts domain data → event, sends to exchange |
| `RabbitMqConfig` | Exchange, queue, binding, `Jackson2JsonMessageConverter` |
| `MealEntryPocListener` | `@RabbitListener` on `meal-entry.poc`, logs full payload at `INFO` |
| `MealEntryCreatedListener` (optional) | Calls publisher after commit from `MealEntryService` or via domain event |

Keep messaging types out of REST DTOs; map explicitly in the publisher.

## POC consumer behavior

`MealEntryPocListener`:

1. Receive `MealEntryCreatedEvent`.
2. Log at `INFO`: `eventId`, `mealEntryId`, `eatenAt`, `dishName`, `nutritionTotals`.
3. Ack message (default auto-ack or manual ack once stable).

No MongoDB, no database writes, no retries beyond Spring AMQP defaults.

## Error handling (POC)

| Failure | Behavior |
|---------|----------|
| Broker down on publish | Log error; meal entry still saved in MySQL. POC may lose events (acceptable); phase 2 → outbox |
| Consumer throws | Let container requeue / DLQ policy; POC listener should not throw |
| Malformed JSON | Dead-letter or log and reject; should not happen if publisher and consumer share the same Java type |

## Implementation phases

### Phase 1 — POC (this document)

- [x] `docker-compose.yml` with RabbitMQ
- [x] `spring-boot-starter-amqp` + `RabbitMqConfig`
- [x] `MealEntryEventPublisher` + after-commit hook on create
- [x] Nutrition calculation for event payload (`MealEntryNutritionCalculator`)
- [x] Fanout exchange + `meal-entry.poc` queue + INFO listener
- [ ] Manual test: create meal entry → see log line and message in Management UI

### Phase 2 — Fanout expansion

- [ ] Add `meal-entry.audit` and `meal-entry.nutrition` queues (same exchange)
- [ ] Implement consumers per linked proposals (MongoDB)
- [ ] Idempotency on `eventId`

### Phase 3 — Hardening

- [ ] Transactional outbox table + relay worker
- [ ] `MealEntryDeleted` (and optional `MealEntryUpdated`) events
- [ ] Dead-letter exchange (DLX) and retry limits
- [ ] Test profile with Testcontainers RabbitMQ or mock publisher

## References in codebase

- Meal create: `MealEntryService.create`
- Resolved composition: `MealEntryService.getResolvedById`, `MealEntryRecipeCompositionResolver`
- Recipe-level nutrition: `RecipeService` (`computeBatchTotals`, per-100g scaling)
- REST resolved shape: `MealEntryResolvedResponseDto`, `MealEntryResolvedRecipeResponseDto`
