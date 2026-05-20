# recipe-api

REST API for managing recipes, ingredients, dishes, and a personal meal log with optional per-meal adjustments and nutrition estimates.

## Features

- **Catalog:** ingredients (with per-100g macros), recipes (components, steps, tags), dishes (linked recipes).
- **Meal log:** record what you ate (`meal_entry`), portions per recipe, and ADD/REMOVE adjustments to components.
- **Resolved meals:** expand a logged meal to the actual ingredient quantities consumed.
- **Nutrition:** recipe batch totals and per-meal macro calculation from resolved composition.
- **Messaging (POC):** after creating a meal entry, publishes a `MealEntryCreated` event to RabbitMQ (fanout) with a denormalized snapshot and nutrition totals.
- **API docs:** OpenAPI via springdoc; custom Swagger UI at `/docs.html`.

## Tech stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.4 |
| API | Spring Web, Bean Validation |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Migrations | Flyway (`src/main/resources/db/migration/`) |
| Messaging | RabbitMQ (Spring AMQP) |
| Docs | springdoc-openapi |
| Build | Gradle (wrapper included) |
| Tests | JUnit 5, Spring Boot Test, H2 (test profile) |

## Prerequisites

- **JDK 21**
- **MySQL 8** running locally (or reachable network) with database `recipe_api`
- **Docker Desktop** (or Docker Engine) — required for local RabbitMQ
- Environment variables for MySQL credentials (see below)

## Getting started

### 1. Database (MySQL)

Create the database if it does not exist:

```sql
CREATE DATABASE recipe_api CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Set credentials (PowerShell example):

```powershell
$env:DB_USER = "your_mysql_user"
$env:DB_PASSWORD = "your_mysql_password"
```

Default JDBC URL: `jdbc:mysql://localhost:3306/recipe_api` (see `application.properties`).

Flyway runs migrations on application startup.

### 2. RabbitMQ (Docker)

The app expects RabbitMQ when `app.rabbitmq.enabled=true` (default in local `application.properties`).

**Debug / IDE (this repo’s `docker-compose.yml`):** broker dedicated to running the API on the host (Cursor, `bootRun`, etc.).

```bash
docker compose up -d rabbitmq
```

| Service | URL / port |
|---------|------------|
| AMQP | `localhost:5672` |
| Management UI | http://localhost:15672 |
| Default user / password | `guest` / `guest` |

**Full stack in Docker (`recipe-infra`):** separate broker on host ports **5673** / **15673** so it does not clash with debug. The API container uses Spring profile **`docker`** (`application-docker.properties`: host `rabbitmq`, port `5672` on the Compose network). See the `recipe-infra` README.

**Check status (debug broker):**

```bash
docker compose ps
```

On first startup the app declares:

- Fanout exchange: `meal-entry.events`
- Queue: `meal-entry.poc` (POC consumer logs events at `INFO`)

**Stop RabbitMQ** (keep data volume):

```bash
docker compose down
```

**Stop and remove the data volume:**

```bash
docker compose down -v
```

**Run without RabbitMQ:** set `app.rabbitmq.enabled=false` in `application.properties` (or an override). The API starts without a broker; meal entries are still saved, but no events are published.

### 3. Run the application

```bash
# Windows
.\gradlew bootRun

# Linux / macOS
./gradlew bootRun
```

Default base URL: http://localhost:8080

**Build and test:**

```bash
.\gradlew build
```

Tests use the `test` profile (H2 in-memory, RabbitMQ auto-configuration disabled).

### 4. Verify messaging (optional)

1. Ensure RabbitMQ is up (`docker compose ps`).
2. Start the API with `app.rabbitmq.enabled=true`.
3. Create a meal entry: `POST /api/meal-entries` (see API docs).
4. In application logs, look for:
   ```text
   Meal entry created event consumed: eventId=..., mealEntryId=..., ...
   ```
5. In the management UI → **Queues** → `meal-entry.poc` → inspect messages.

## Configuration

| Property / variable | Description |
|---------------------|-------------|
| `DB_USER` | MySQL username (required) |
| `DB_PASSWORD` | MySQL password (required) |
| `spring.datasource.url` | JDBC URL (default `localhost:3306/recipe_api`) |
| `spring.rabbitmq.host` | Rabbit host (default `localhost`) |
| `spring.rabbitmq.port` | Rabbit port (default `5672`) |
| `app.rabbitmq.enabled` | `true` = publish and consume; `false` = no-op publisher, no listener |

## API documentation

| URL | Description |
|-----|-------------|
| http://localhost:8080/docs.html | Swagger UI (dark theme) |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |
| http://localhost:8080/swagger-ui.html | springdoc default UI |

### Main REST prefixes

| Path | Resource |
|------|----------|
| `/api/ingredients` | Ingredients |
| `/api/recipes` | Recipes |
| `/api/dishes` | Dishes |
| `/api/meal-entries` | Meal log |

Health-style endpoint: `GET /ping`

## Project layout

```text
src/main/java/com/aleitox/recipe/
  controller/     REST controllers
  service/        Business logic
  repository/     Spring Data JPA
  entity/         JPA entities
  dto/            Request/response DTOs
  mapper/         Entity ↔ domain ↔ DTO
  messaging/      RabbitMQ config, publisher, POC listener
docs/             Design proposals and schema notes
src/main/resources/db/migration/   Flyway SQL
docker-compose.yml                 Local RabbitMQ
```

## Messaging overview

When a meal entry is created and the transaction commits:

1. `MealEntryService` publishes an internal `MealEntryCreatedNotification`.
2. `MealEntryCreatedPublishListener` runs **after commit** and calls `MealEntryEventPublisher`.
3. The publisher loads the **resolved** meal, computes macros, and sends `MealEntryCreated` to exchange `meal-entry.events`.
4. `MealEntryPocListener` on queue `meal-entry.poc` logs the event (POC).

Further design (audit/nutrition consumers, MongoDB): see [`docs/rabbitmq-proposal.md`](docs/rabbitmq-proposal.md), [`docs/consumer-audit-proposal.md`](docs/consumer-audit-proposal.md), [`docs/consumer-nutrition-proposal.md`](docs/consumer-nutrition-proposal.md).

## Documentation

| Document | Topic |
|----------|--------|
| [`docs/database-schema.md`](docs/database-schema.md) | ER diagram and tables |
| [`docs/rabbitmq-proposal.md`](docs/rabbitmq-proposal.md) | RabbitMQ integration |
| [`docs/schema-evolution-proposal.md`](docs/schema-evolution-proposal.md) | Planned schema changes |
| [`docs/dynamic-spicy-proposal.md`](docs/dynamic-spicy-proposal.md) | Future spiciness model |

## Docker image (API)

A multi-stage `Dockerfile` builds a runnable JAR. RabbitMQ and MySQL are expected as separate services when containerizing the full stack locally.

```bash
docker build -t recipe-api .
```

## License

Not specified for this repository.
