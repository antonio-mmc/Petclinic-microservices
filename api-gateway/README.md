# PetClinic — API Gateway

Spring Cloud Gateway instance serving as the unified entry point for the ASID Spring PetClinic stack, demonstrating the **Strangler Fig Pattern** migration from a monolith to 3 independent microservices.

## Architecture

| Service | Port (Local) | Responsibility |
|---|---|---|
| api-gateway | 8000 | Unified entry point (request routing & forwarding) |
| customer-service | 8080 | Owners, Pets, and PetTypes |
| vet-service | 8081 | Vets and Specialties |
| visit-service | 8082 | Visits (synchronously validates `petId` via customer-service) |

**Important:** All external requests should go to `http://localhost:8000` — never to a backend port directly. If a backend port appears to "work" but the gateway doesn't, that indicates a routing bug.

### Inter-Service Communication
- The **visit-service** validates the `petId` remotely against the **customer-service** before creating a visit (synchronous REST call).
- **Circuit Breaker** with Resilience4j: if the customer-service is unavailable, the visit is rejected with a controlled `503` error.

### Observability — OpenTelemetry + Jaeger
- OpenTelemetry Java agent is included in each Docker image.
- The JVM starts with `-javaagent` — no application code changes required.
- Traces are sent via OTLP to a Jaeger all-in-one instance.

---

## End-to-End Walkthrough

### 1. Prerequisites
| Tool | Version | Check |
|---|---|---|
| JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Docker + Compose v2 | recent | `docker compose version` |

*(Ensure Docker Desktop has Kubernetes active if you plan to deploy to k8s for tracing.)*

### 2. Build Images
Each service must be built before starting the stack (Dockerfiles copy the compiled JARs from `target/`):

```bash
for svc in \
  "customer-service:customer-service" \
  "vet-service:vet-service" \
  "visit-service:visit-service" \
  "API-Gateway/api-gateway:api-gateway"; do
  dir="${svc%%:*}"; tag="${svc##*:}"
  (cd ~/ASID/$dir && mvn -q clean package -DskipTests && docker build -t grupo1/$tag:latest .)
done
```

### 3. Start the Stack (Docker Compose)

```bash
cd ~/ASID/API-Gateway
docker compose up -d
docker compose ps
```

*Note: The backend databases are in-memory H2. Restarting containers (`docker compose down && docker compose up -d`) will wipe all data.*

### 4. Smoke Test Routes

**Create an owner:**
```bash
curl -i -X POST http://localhost:8000/api/owners \
  -H "Content-Type: application/json" \
  -d '{"firstName": "George", "lastName": "Franklin", "address": "110 W. Liberty St.", "city": "Madison", "telephone": "6085551023"}'
```

**Fetch the owner:**
```bash
curl -s http://localhost:8000/api/owners/1 | jq .
```

**Create a visit (involves synchronous cross-service validation):**
```bash
curl -i -X POST http://localhost:8000/api/visits \
  -H "Content-Type: application/json" \
  -d '{"petId":1,"visitDate":"2026-06-01","description":"Annual checkup"}'
```

**Test Validation Errors (RFC 7807):**
```bash
curl -i -X POST http://localhost:8000/api/owners \
  -H "Content-Type: application/json" \
  -d '{"firstName":"","lastName":""}'
```

---

## 5. Observability & Tracing (Kubernetes Demo)

This stack supports distributed tracing using OpenTelemetry and Jaeger.

1. **Deploy to Kubernetes:**
   ```bash
   cd ~/ASID/API-Gateway/api-gateway/k8s
   kubectl apply -f jaeger.yaml
   kubectl apply -f api-gateway.yaml
   kubectl apply -f customer-service.yaml
   kubectl apply -f vet-service.yaml
   kubectl apply -f visit-service.yaml
   ```

2. **Expose Services:**
   Open separate terminals:
   ```bash
   kubectl port-forward svc/api-gateway 8000:8000
   kubectl port-forward svc/jaeger 16686:16686
   ```

3. **View Traces:**
   Open **http://localhost:16686**. Select `visit-service` and click "Find Traces". You will see a distributed trace spanning from `api-gateway` → `visit-service` → `customer-service`.

4. **Circuit Breaker Demo:**
   ```bash
   kubectl scale deployment customer-service --replicas=0

   # Attempt to create a visit — should return a controlled 503 instead of crashing:
   curl -i -X POST http://localhost:8000/api/visits \
     -H "Content-Type: application/json" \
     -d '{"petId":1,"visitDate":"2026-06-01","description":"Circuit breaker test"}'

   kubectl scale deployment customer-service --replicas=1
   ```

---

## 6. Known Issues

**`Location` Header Returns Internal Hostname**
If the `Location` header after a `POST` returns an internal hostname (e.g., `http://customer-service:8080/api/owners/3`), ensure `server.forward-headers-strategy: framework` is set in the backend services' `application.yml`.
