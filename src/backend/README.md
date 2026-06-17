# Workflow Net Backend

Document sequential approval flow API. Part of the **app-workflow-net-angular** project.

## Tech Stack

- **Spring Boot 3.4.2** with Java 17 / Kotlin 2.1
- **Gradle** (Kotlin DSL)
- **MongoDB 8** — document and event store
- **Apache Kafka 4.3.0** — event bus for SAGA orchestration

## Architecture

**Vertical Slices + Clean Architecture** with SAGA orchestration over Kafka:

```
flow/          — Flow creation, approval, rejection
auth/          — GitHub OAuth + JWS token handling
audit/         — Append-only audit log
document/      — Temporary document storage
admin/         — Admin dashboard endpoints
shared/        — Base entities, events, utilities
config/        — Application configuration
```

## How to Run Locally

### Prerequisites

- JDK 17+
- Docker Desktop

### Steps

1. **Start infrastructure:**
   ```bash
   docker-compose -f ../../docker-compose.yml up -d mongodb kafka
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```
   On Windows:
   ```cmd
   gradlew.bat bootRun
   ```

3. Open **http://localhost:8080**

4. Swagger UI: **http://localhost:8080/swagger-ui.html**

## Configuration

Uses the project root `.env` file. Key properties:

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | JWS signing secret |
| `MONGODB_URI` | MongoDB connection string |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address |
| `GITHUB_CLIENT_ID` | GitHub OAuth app client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth app secret |

## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/actuator/health` | Health check |
| GET | `/actuator/prometheus` | Prometheus metrics |
| POST | `/api/flows` | Create a new approval flow |
| GET | `/api/flows` | List flows |
| POST | `/api/flows/{id}/approve/{step}` | Approve a flow step |
| POST | `/api/documents/upload` | Upload a document |
| GET | `/api/auth/profile` | Get authenticated user profile |

## Package Structure

```
com.workflowspring
├── config/          — Configuration classes (security, kafka, mongo, etc.)
├── flow/            — Flow vertical slice
│   ├── domain/      — Entities, value objects, domain events
│   ├── application/ — Use cases, services, DTOs
│   ├── infrastructure/ — Persistence, Kafka producers/consumers
│   └── presentation/   — REST controllers
├── auth/            — Authentication vertical slice
├── audit/           — Audit log vertical slice
├── document/        — Document management vertical slice
├── admin/           — Admin endpoints vertical slice
└── shared/          — Shared kernel (base entities, events, utilities)
```
