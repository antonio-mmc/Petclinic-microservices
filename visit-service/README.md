# PetClinic Visit Service

## Description

**Bounded Context:** Pet Care & Scheduling Domain

The Visit Service manages the full lifecycle of pet visit records. Before persisting a new visit it synchronously validates the pet's existence and active status against the Customer Service. It also consumes RabbitMQ events to automatically cancel visits when a pet is deactivated.

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.x |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Messaging | Spring Cloud Stream — RabbitMQ binder |
| Resilience | Resilience4j — Circuit Breaker |
| Observability | Micrometer + OpenTelemetry (OTLP) |
| API Docs | SpringDoc OpenAPI 2.3.0 (Swagger UI) |

## Architecture

### Communication strategy
- **Synchronous REST** — before creating a visit, the service calls `/api/internal/pets/{petId}` on the Customer Service to verify the pet exists and is `ACTIVE`. This call is wrapped in a Resilience4j Circuit Breaker.
- **Asynchronous events (RabbitMQ)** — the service consumes `PetDeactivatedEvent` to cancel any scheduled visits for a deactivated pet.

### Resilience
The `PetValidationClient` is protected by a **Resilience4j Circuit Breaker** (`petService`):
- If the Customer Service is unavailable, the circuit trips and returns a `503 Service Unavailable`.
- Business exceptions (`404 pet not found`, `409 pet inactive`) are declared as `ignoreExceptions` so they do not count as circuit breaker failures and are propagated with their original status codes.

### Error handling
All errors follow **RFC 7807** (`ProblemDetail`):
- `400 Bad Request` — pet does not exist.
- `409 Conflict` — pet is `INACTIVE`.
- `404 Not Found` — visit does not exist.
- `503 Service Unavailable` — Customer Service is unreachable (circuit open).

## API Endpoints

| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/visits` | List all visits. |
| `GET` | `/api/visits/{visitId}` | Get a visit by ID. |
| `POST` | `/api/visits` | Create a visit (validates pet before saving). |
| `PUT` | `/api/visits/{visitId}` | Update a visit's date and description. |
| `DELETE` | `/api/visits/{visitId}` | Delete a visit by ID. |
| `GET` | `/api/visits/pets/{petId}` | List all visits for a specific pet. |

### Request body for `POST` / `PUT`
```json
{
  "petId": 1,
  "visitDate": "2026-06-01",
  "description": "Annual checkup"
}
```

## How to Run

### Prerequisites
- Java 21
- Maven 3.9+
- Docker (for RabbitMQ)
- Customer Service running on port 8080

### 1. Start infrastructure
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Run locally
```bash
./mvnw spring-boot:run
```
The service starts on **port 8082** and connects to the local RabbitMQ instance, an in-memory H2 database, and the Customer Service at `http://localhost:8080`.

> In the full stack, all external traffic must go through the API Gateway on port **8000**.

### 3. API Documentation
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- OpenAPI spec: `http://localhost:8082/v3/api-docs`

## Testing

Tests use `@WebMvcTest` (controller slice only). `VisitRepository`, `VisitMapper`, and `PetValidationClient` are mocked. Tests are grouped with `@Nested` inner classes per operation.

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=VisitControllerTest

# Run a specific test method
mvn test -Dtest=VisitControllerTest#shouldCreateVisit
```

## Full-Stack Deployment

Use Docker Compose from the `API-Gateway` directory:

```bash
cd ~/ASID/API-Gateway
docker compose up -d
```

See the API Gateway README for the complete build and deploy walkthrough.
