# Spring PetClinic Microservices

A full-stack reference implementation demonstrating a **Strangler Fig Pattern** migration from a monolith to independent microservices, built as part of the ASID (Architectures and Systems Integration and Distribution) course.

The classic [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) application is decomposed into three bounded-context microservices, fronted by a Spring Cloud Gateway and an Angular SPA.

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│               Browser / API Client                        │
└──────────────────────────┬───────────────────────────────┘
                            │ :4200 (Angular) / :8000 (API)
                            ▼
                   ┌─────────────────┐
                   │   API Gateway   │  Spring Cloud Gateway
                   │   (port 8000)   │  CORS, routing, load balancing
                   └────────┬────────┘
                            │
           ┌────────────────┼────────────────┐
           │                │                │
           ▼                ▼                ▼
  ┌─────────────────┐ ┌──────────────┐ ┌─────────────────┐
  │ customer-service│ │  vet-service │ │  visit-service  │
  │   (port 8080)   │ │ (port 8081)  │ │  (port 8082)    │
  │                 │ │              │ │                  │
  │ Owner, Pet,     │ │ Vet,         │ │ Visit            │
  │ PetType domains │ │ Specialty    │ │ (validates Pet   │
  └────────┬────────┘ └──────────────┘ │  before saving) │
           │                           └────────┬────────┘
           │ ◄─── REST: GET /api/internal/pets ──┘
           │
      ┌────▼────────┐
      │  RabbitMQ   │  pet-events exchange
      └─────────────┘
           │
           └──────────────────────► visit-service
                                    (cancels visits on
                                     PetDeactivatedEvent)
```

### Service Map

| Service | Port | Domain | Key Capabilities |
|---|---|---|---|
| `api-gateway` | 8000 | Routing | Spring Cloud Gateway, CORS, service discovery, OTLP traces |
| `customer-service` | 8080 | Owner, Pet, PetType | Full CRUD, pet deactivation, event publishing |
| `vet-service` | 8081 | Vet, Specialty | Full CRUD, pre-seeded data |
| `visit-service` | 8082 | Visit | Cross-service validation, circuit breaker, event consumer |
| `front-end` | 4200 | Angular SPA | Full UI for all domains |

---

## Technology Stack

### Backend
| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.x |
| API Gateway | Spring Cloud Gateway 2023.0.0 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Async Messaging | Spring Cloud Stream 4 + RabbitMQ binder |
| Resilience | Resilience4j — Circuit Breaker + Time Limiter |
| API Docs | SpringDoc OpenAPI 2.3.0 (Swagger UI) |
| Observability | Micrometer Tracing + OpenTelemetry OTLP → Jaeger |
| Containerization | Docker + Docker Compose / Kubernetes |

### Frontend
| Concern | Technology |
|---|---|
| Framework | Angular 16 |
| Language | TypeScript |
| Styling | Bootstrap 3 |
| HTTP | Angular HttpClient |

---

## Getting Started

### Prerequisites

| Tool | Minimum Version |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Docker + Compose v2 | recent |
| Node.js *(frontend only)* | 18+ |

### 1. Build all Docker images

Run this from the repository root:

```bash
for svc in customer-service vet-service visit-service api-gateway; do
  (cd $svc && mvn -q clean package -DskipTests && docker build -t grupo1/$svc:latest .)
done
```

### 2. Start the full stack

```bash
docker compose up -d
docker compose ps
```

> **Note:** All databases are in-memory H2. Restarting containers (`docker compose down && docker compose up -d`) wipes all data.

### 3. Start the Angular front-end (optional)

```bash
cd front-end
npm install
ng serve
```

Navigate to `http://localhost:4200`. The app connects to the API Gateway at `http://localhost:8000`.

---

## Access Points

| Interface | URL | Notes |
|---|---|---|
| Angular App | http://localhost:4200 | Requires `ng serve` |
| API Gateway | http://localhost:8000 | Entry point for all API calls |
| Jaeger UI | http://localhost:16686 | Distributed traces |
| RabbitMQ UI | http://localhost:15672 | guest / guest |
| Customer Swagger | http://localhost:8080/swagger-ui.html | Direct service access |
| Vet Swagger | http://localhost:8081/swagger-ui.html | Direct service access |
| Visit Swagger | http://localhost:8082/swagger-ui.html | Direct service access |

> All API traffic in production should go through the gateway on port **8000**.

---

## API Reference

All routes are accessible through the API Gateway at `http://localhost:8000`.

### Customer Service

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/owners` | List all owners (`?lastName=` filter) |
| `GET` | `/api/owners/{id}` | Get owner with their pets |
| `POST` | `/api/owners` | Create a new owner |
| `PUT` | `/api/owners/{id}` | Update an owner |
| `DELETE` | `/api/owners/{id}` | Delete an owner |
| `POST` | `/api/owners/{id}/pets` | Add a pet to an owner |
| `GET` | `/api/pets/{id}` | Get a pet by ID |
| `PATCH` | `/api/pets/{id}/deactivate` | Deactivate a pet (publishes event) |
| `GET` | `/api/petTypes` | List all pet types |

### Vet Service

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/vets` | List all vets with specialties |
| `GET` | `/api/vets/{id}` | Get a vet by ID |
| `POST` | `/api/vets` | Create a vet |
| `PUT` | `/api/vets/{id}` | Update a vet |
| `DELETE` | `/api/vets/{id}` | Delete a vet |
| `GET` | `/api/specialties` | List all specialties |
| `POST` | `/api/specialties` | Create a specialty |
| `PUT` | `/api/specialties/{id}` | Update a specialty |
| `DELETE` | `/api/specialties/{id}` | Delete a specialty |

### Visit Service

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/visits` | List all visits (paginated) |
| `GET` | `/api/visits/{id}` | Get a visit by ID |
| `POST` | `/api/visits` | Create a visit (validates pet first) |
| `PUT` | `/api/visits/{id}` | Update a visit |
| `DELETE` | `/api/visits/{id}` | Delete a visit |
| `GET` | `/api/visits/pets/{petId}` | All visits for a specific pet |

---

## Key Design Patterns & Architectural Decisions

### 1. Strangler Fig Pattern
Each service is a self-contained bounded context with its own in-memory database. There are no cross-service database joins. Services own their domain data exclusively.

### 2. Synchronous Validation with Circuit Breaker

When creating a visit, `visit-service` calls `customer-service` to verify the pet exists and is `ACTIVE`:

```
POST /api/visits
    └──► GET /api/internal/pets/{petId}   (customer-service)
             ├── 200 ACTIVE  → proceed, save visit
             ├── 404         → 400 Bad Request (pet not found)
             ├── 409 INACTIVE → 409 Conflict (pet is inactive)
             └── timeout/5xx → Circuit Breaker opens → 503 Service Unavailable
```

The Resilience4j circuit breaker is configured with `ignoreExceptions` for `PetValidationException` and `InactivePetException` so business errors never falsely trip the circuit.

### 3. Eventual Consistency via RabbitMQ

```
PATCH /api/pets/{id}/deactivate   (customer-service)
    └──► publishes PetDeactivatedEvent { petId, timestamp }
             └──► visit-service consumes event
                      └──► cancels all SCHEDULED visits for petId
```

This decouples the deactivation flow — `customer-service` does not need to know about visits.

### 4. RFC 7807 Error Handling
All services return `ProblemDetail` objects (RFC 7807) via a `@RestControllerAdvice`. Error codes are meaningful:

| HTTP Status | Meaning |
|---|---|
| 400 | Validation failure or bad pet reference |
| 404 | Resource does not exist |
| 409 | Business invariant violation (e.g., operating on an INACTIVE pet) |
| 503 | Downstream service unavailable (circuit open) |

---

## Observability

### Distributed Tracing (OpenTelemetry + Jaeger)

Each service is instrumented with the OpenTelemetry Java agent. Traces are exported via OTLP HTTP to Jaeger.

After starting the stack, open **http://localhost:16686** and select any service to see end-to-end traces, including the cross-service call from `visit-service` → `customer-service`.

### Kubernetes Deployment

```bash
cd api-gateway/k8s
kubectl apply -f jaeger.yaml
kubectl apply -f customer-service.yaml
kubectl apply -f vet-service.yaml
kubectl apply -f visit-service.yaml
kubectl apply -f api-gateway.yaml

# Port-forward to access locally
kubectl port-forward svc/api-gateway 8000:8000
kubectl port-forward svc/jaeger 16686:16686
```

---

## Circuit Breaker Demo

```bash
# 1. Scale customer-service to zero to simulate outage
kubectl scale deployment customer-service --replicas=0

# 2. Attempt visit creation — should return 503, not an unhandled crash
curl -i -X POST http://localhost:8000/api/visits \
  -H "Content-Type: application/json" \
  -d '{"petId":1,"visitDate":"2026-06-01","description":"Circuit breaker test"}'

# 3. Restore the service
kubectl scale deployment customer-service --replicas=1
```

---

## Repository Structure

```
.
├── api-gateway/           # Spring Cloud Gateway — unified entry point (port 8000)
│   ├── src/
│   ├── k8s/               # Kubernetes manifests
│   ├── Dockerfile
│   └── pom.xml
├── customer-service/      # Owner, Pet, PetType bounded context (port 8080)
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── vet-service/           # Vet, Specialty bounded context (port 8081)
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── visit-service/         # Visit bounded context (port 8082)
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── front-end/             # Angular 16 SPA (port 4200)
│   ├── src/
│   ├── Dockerfile
│   └── package.json
└── docker-compose.yml     # Full stack orchestration (all services + RabbitMQ + Jaeger)
```

---

## Testing

Each service has its own unit/integration test suite. Run per module:

```bash
mvn test -pl customer-service   # 8 tests
mvn test -pl visit-service      # 13 tests
mvn test -pl vet-service        # 4 tests
mvn test -pl api-gateway        # 7 tests
```

| Service | Test Class | Pattern | Coverage |
|---|---|---|---|
| `customer-service` | `OwnerControllerTest` | `@WebMvcTest` + `@MockBean` | GET owner (200/404), POST pet (201/400/404) |
| `customer-service` | `InternalPetControllerTest` | `@WebMvcTest` + `@MockBean` | GET pet (200/404), status validation (409) |
| `visit-service` | `VisitControllerTest` | `@WebMvcTest` + `@MockBean` | GET/POST/PUT/DELETE visits, 409 conflict |
| `visit-service` | `PetEventConsumerTest` | `@ExtendWith(MockitoExtension)` | PetDeactivatedEvent: deleteAll / skip |
| `vet-service` | `VetResourceTest` | `@WebMvcTest` + `@MockBean` | GET all, GET by ID (200/404), POST |
| `api-gateway` | `GatewayRoutesTest` | `@SpringBootTest` | `lb://` URI per service, 3 routes |

---

## Smoke Test Sequence

```bash
# 1. Create an owner
curl -i -X POST http://localhost:8000/api/owners \
  -H "Content-Type: application/json" \
  -d '{"firstName":"George","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}'

# 2. Add a pet to the owner (ownerId=1, get petType IDs from GET /api/petTypes)
curl -i -X POST http://localhost:8000/api/owners/1/pets \
  -H "Content-Type: application/json" \
  -d '{"name":"Leo","birthDate":"2020-09-07","typeId":1}'

# 3. Create a visit (petId=1 — validates against customer-service)
curl -i -X POST http://localhost:8000/api/visits \
  -H "Content-Type: application/json" \
  -d '{"petId":1,"visitDate":"2026-06-15","description":"Annual checkup"}'

# 4. Deactivate the pet (triggers RabbitMQ event → visit-service cancels visits)
curl -i -X PATCH http://localhost:8000/api/pets/1/deactivate

# 5. Attempt to create a visit for the deactivated pet — expect 409 Conflict
curl -i -X POST http://localhost:8000/api/visits \
  -H "Content-Type: application/json" \
  -d '{"petId":1,"visitDate":"2026-07-01","description":"This should fail"}'
```
