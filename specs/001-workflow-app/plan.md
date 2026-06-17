# Implementation Plan: Document Workflow Application

**Branch**: `001-workflow-app` | **Date**: 2026-06-15 | **Spec**: [spec.md](spec.md)

## Summary

Build a document sequential approval workflow system with a Spring Boot (Kotlin) backend and Angular frontend. The system allows admins to upload documents, assign sequential recipients, and track approvals via email with JWS tokens. Uses MongoDB for persistence, Kafka for event-driven orchestration (SAGA pattern), and Docker for deployment.

## Technical Context

**Language/Version**: Java 17 (backend), Kotlin 2.1, TypeScript (Angular 22)

**Primary Dependencies**:
- Backend: Spring Boot 3.4.2, Spring Data MongoDB, Spring Security OAuth2, Spring Kafka, jjwt 0.12.6
- Frontend: Angular 22, Tailwind CSS, rxjs
- Infrastructure: MongoDB 8, Kafka 4.3.0, Traefik, Grafana, Prometheus

**Storage**: MongoDB (flow data, documents, audit log), Temporary file system (document storage)

**Testing**: JUnit 5 + MockK (backend unit tests), Jest (frontend unit tests), Spring Boot Test (integration)

**Target Platform**: Linux VPS via Docker Compose

**Project Type**: Full-stack web application (backend API + Angular SPA)

**Performance Goals**: <200ms API response time for approval actions, <30s email delivery

**Constraints**: Documents max 2MB each, max 5 per flow, 3-day grace period after deadline

**Scale/Scope**: MVP supports single-tenant deployment with multiple admin users

## Constitution Check

*GATE: Must pass before implementation.*

- [x] Clean Architecture & Vertical Slices — organized by feature (flow, auth, audit, document)
- [x] Event-Driven Orchestration (SAGA) — FlowOrchestratorService as central coordinator
- [x] TDD-First — tests written before implementation code
- [x] Security-First — JWS tokens, GitHub OAuth, append-only audit log
- [x] Observability — healthcheck, Prometheus metrics, audit logging, email DLQ

## Project Structure

### Documentation

```
specs/001-workflow-app/
├── spec.md                # This file (feature specification)
├── plan.md                # This file (implementation plan)
├── data-model.md          # Data model definitions
├── tasks.md               # Task breakdown (/speckit.tasks output)
├── checklists/
│   └── requirements.md    # Quality checklist
└── contracts/
    ├── api-spec.json      # OpenAPI specification
    └── event-catalog.md   # Kafka event definitions
```

### Source Code

```
src/
├── backend/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── Dockerfile
│   ├── src/main/kotlin/com/workflownet/
│   │   ├── WorkflowNetApplication.kt
│   │   ├── config/
│   │   │   ├── MongoConfig.kt
│   │   │   ├── KafkaConfig.kt
│   │   │   ├── SecurityConfig.kt
│   │   │   ├── CorsConfig.kt
│   │   │   └── OpenApiConfig.kt
│   │   ├── flow/
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   ├── infrastructure/
│   │   │   └── presentation/
│   │   ├── auth/
│   │   ├── audit/
│   │   ├── document/
│   │   ├── admin/
│   │   └── shared/
│   └── src/test/kotlin/com/workflownet/
│
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── proxy.conf.json
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   ├── pages/
│   │   │   ├── services/
│   │   │   ├── models/
│   │   │   ├── guards/
│   │   │   └── interceptors/
│   │   ├── assets/
│   │   └── environments/
│   └── package.json
│
└── docker/
    ├── docker-compose.yml
    ├── mongodb/
    ├── grafana/
    ├── prometheus/
    └── traefik/
```

### Architecture Decision: Vertical Slices over Layered Architecture

Each feature (flow, auth, audit, document, admin) contains its own domain, application, infrastructure, and presentation layers. This ensures:
- Features can be developed and tested independently
- No cross-contamination between feature boundaries
- Easier to locate and modify feature-specific code
- Alignment with the SAGA orchestration pattern (the orchestrator coordinates across slices)

## Implementation Phases

### Phase 1: Foundation (Infrastructure + Backend Skeleton)
1. Gradle build configuration with all dependencies
2. MongoDB configuration and connection
3. Kafka configuration and topic creation
4. Security configuration (GitHub OAuth + JWT filter)
5. Main application class with scheduling enabled
6. Docker Compose for development services (MongoDB, Kafka)
7. Environment configuration (.env, application.yml)

### Phase 2: Domain Models & Persistence
1. Domain entities (Flow, DocumentMetadata, Participant, FlowAuditLog, TempDocument)
2. Domain events (FlowCreated, FlowStarted, DocumentApproved, DocumentRejected, etc.)
3. Spring Data MongoDB repositories
4. Idempotency consumer infrastructure
5. Database indexes (TTL, unique constraints)

### Phase 3: SAGA Orchestration & Kafka Integration
1. FlowOrchestratorService (core SAGA coordinator)
2. FlowEventPublisher (Kafka producer)
3. FlowEventConsumer (Kafka consumer with idempotency)
4. EmailSenderService (with retry + DLQ configuration)
5. FlowDeadlineService (scheduled expiration check)

### Phase 4: Application Services & REST API
1. FlowService (CRUD + business operations)
2. DocumentService (upload, hash, temp storage)
3. AuthService & JwtTokenService (JWS generation/validation)
4. AuditService (append-only logging)
5. FlowRepairService (corrupted flow recovery)
6. REST controllers (FlowController, DocumentController, AuthController, AuditController, AdminController)

### Phase 5: Frontend Application
1. Angular project initialization with Tailwind CSS
2. Authentication module (GitHub OAuth callback, login state)
3. Admin dashboard with KPI widgets
4. Flow management (create flow, upload documents, add recipients)
5. Master grid with search, sort, filter, pagination (URL state)
6. Recipient view (pending flows, approve/reject actions)
7. API service layer and interceptors

### Phase 6: Infrastructure & Deployment
1. Docker Compose (full stack)
2. Dockerfiles (multi-stage builds for backend and frontend)
3. Traefik configuration
4. Grafana dashboards + Prometheus configuration
5. GitHub Actions CI/CD workflow
6. start-dev.bat script
7. README.md and AGENTS.md documentation

## Complexity Tracking

No constitution violations. All architectural decisions follow established principles.
