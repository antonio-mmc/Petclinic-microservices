# PetClinic Vet Service

## Description

**Bounded Context:** Veterinarian / Staff Domain

The Vet Service is the authoritative source of truth for **Vets** and **Specialties**. It manages the full lifecycle of veterinarian profiles and their clinical specializations. This service is intentionally isolated — it does not communicate with other services and owns its own database.

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.x |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Observability | Micrometer + OpenTelemetry (OTLP) |
| API Docs | SpringDoc OpenAPI 2.3.0 (Swagger UI) |

## Architecture

### Communication strategy
The Vet Service exposes only synchronous REST endpoints. It does not publish or consume messages — the vet domain has no cross-service state dependencies.

### Error handling
All errors follow **RFC 7807** (`ProblemDetail`):
- `404 Not Found` — vet or specialty does not exist.
- `400 Bad Request` — validation failure on request body.

## API Endpoints

### Vets

| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/vets` | List all vets with their specialties. |
| `GET` | `/api/vets/{vetId}` | Get a vet by ID. |
| `POST` | `/api/vets` | Create a new vet. |
| `PUT` | `/api/vets/{vetId}` | Update a vet (name and specialties). |
| `DELETE` | `/api/vets/{vetId}` | Delete a vet by ID. |

### Specialties

| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/specialties` | List all specialties. |
| `GET` | `/api/specialties/{specialtyId}` | Get a specialty by ID. |
| `POST` | `/api/specialties` | Create a new specialty. |
| `PUT` | `/api/specialties/{specialtyId}` | Update a specialty name. |
| `DELETE` | `/api/specialties/{specialtyId}` | Delete a specialty by ID. |

### Seed data
On startup the service pre-loads 6 vets and 3 specialties (radiology, surgery, dentistry). New records created via the API will start from ID 10 onwards.

## How to Run

### Prerequisites
- Java 21
- Maven 3.9+

### Run locally
```bash
./mvnw spring-boot:run
```
The service starts on **port 8081** with an in-memory H2 database.

> In the full stack, all external traffic must go through the API Gateway on port **8000**.

### API Documentation
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI spec: `http://localhost:8081/v3/api-docs`

## Full-Stack Deployment

Use Docker Compose from the `API-Gateway` directory:

```bash
cd ~/ASID/API-Gateway
docker compose up -d
```

See the API Gateway README for the complete build and deploy walkthrough.
