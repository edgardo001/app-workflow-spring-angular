# Task Breakdown: Document Workflow Application

**Source**: [plan.md](plan.md) | **Date**: 2026-06-15

---

## Phase 1: Foundation

### Task 1.1 — Gradle Build Configuration
**Files**: `src/backend/build.gradle.kts`, `src/backend/settings.gradle.kts`
- [x] Configure Spring Boot 3.4.2 plugin
- [x] Configure Kotlin 2.1 JVM plugin
- [x] Add dependencies: spring-boot-starter-web, data-mongodb, security, oauth2-client, mail, kafka, actuator
- [x] Add jjwt 0.12.6 for JWS tokens
- [x] Add springdoc-openapi for OpenAPI/Swagger
- [x] Add micrometer-registry-prometheus
- [x] Add test dependencies: mockk, spring-kafka-test

### Task 1.2 — Application Configuration
**Files**: `src/backend/src/main/resources/application.yml`
- [x] Server port, MongoDB URI, Kafka bootstrap servers
- [x] Kafka producer/consumer config with JSON serialization
- [x] Mail configuration
- [x] JWT secret and expiration
- [x] Document limits (max 2MB, max 5)
- [x] Flow grace period (3 days)
- [x] Email retry configuration (max 5, backoff)
- [x] Actuator endpoints (health, prometheus, metrics)
- [x] OpenAPI/Swagger UI enabled
- [x] Logging levels

### Task 1.3 — Main Application Class & Config
**Files**: `src/backend/src/main/java/com/workflownet/WorkflowNetApplication.java`
- [x] `@SpringBootApplication`, `@EnableScheduling`

### Task 1.4 — Infrastructure Configuration
**Files**: `src/backend/src/main/java/com/workflownet/config/*`
- [x] MongoConfig: MongoTemplate, auto-index-creation
- [x] KafkaConfig: JSON serde, retry topics, DLQ, retry backoff
- [x] SecurityConfig: GitHub OAuth2 login, permit auth/actuator/openapi paths, JWT filter
- [x] CorsConfig: allow frontend origin (localhost:4200)
- [x] OpenApiConfig: title, description, version, security scheme

### Task 1.5 — Environment & Docker Foundation
**Files**: `.env.example`, `src/docker/docker-compose.yml`
- [x] Create comprehensive `.env.example` with all variables commented
- [x] Docker Compose with mongodb, kafka, zookeeper services
- [x] MongoDB init script with user creation and indexes

---

## Phase 2: Domain Models & Persistence

### Task 2.1 — Shared Components
**Files**: `src/backend/src/main/java/com/workflownet/shared/model/BaseEntity.java`, `shared/util/IdGenerator.java`
- [x] BaseEntity with id (UUID), createdAt, updatedAt
- [x] IdGenerator utility

### Task 2.2 — Flow Domain Model
**Files**: `src/backend/src/main/java/com/workflownet/flow/domain/model/*`
- [x] FlowStatus enum (DRAFT, ACTIVE, PENDING_APPROVAL, COMPLETED, REJECTED, EXPIRED, CANCELLED)
- [x] Flow aggregate root with: id, title, description, status, documents, participants, currentStep, deadline, createdBy, lifecycles methods
- [x] DocumentMetadata value object
- [x] Participant value object
- [x] IdempotencyKey for consumer idempotency

### Task 2.3 — Domain Events
**Files**: `src/backend/src/main/java/com/workflownet/flow/domain/event/*`
- [x] FlowEvent interface
- [x] FlowCreatedEvent, FlowStartedEvent, DocumentApprovedEvent, DocumentRejectedEvent
- [x] FlowExpiredEvent, FlowCompletedEvent
- [x] EmailSendEvent, EmailFailedEvent (shared events)

### Task 2.4 — Audit & Document Models
**Files**: `src/backend/src/main/java/com/workflownet/audit/domain/FlowAuditLog.java`
- [x] FlowAuditLog with: id, flowId, action, userId, userEmail, timestamp, documentHash, metadata
**Files**: `src/backend/src/main/java/com/workflownet/document/domain/TempDocument.java`
- [x] TempDocument with TTL index, byte[] data, expiresAt

### Task 2.5 — Repositories
**Files**: `src/backend/src/main/java/com/workflownet/**/persistence/*`
- [x] FlowRepository (Spring Data MongoDB)
- [x] FlowMongoRepository (wrapper with pagination)
- [x] IdempotencyRepository (unique key constraint)
- [x] AuditLogRepository
- [x] TempDocumentRepository

---

## Phase 3: SAGA Orchestration & Kafka

### Task 3.1 — Event Publisher
**Files**: `src/backend/src/main/java/com/workflownet/flow/infrastructure/messaging/FlowEventPublisher.java`
- [x] KafkaTemplate-based publisher for all domain events
- [x] Topic mapping for each event type

### Task 3.2 — Event Consumer (Idempotent)
**Files**: `src/backend/src/main/java/com/workflownet/flow/infrastructure/messaging/FlowEventConsumer.java`
- [x] Kafka listeners for flow.* events
- [x] Idempotency check before processing
- [x] @RetryableTopic for email-related consumers

### Task 3.3 — SAGA Orchestrator
**Files**: `src/backend/src/main/java/com/workflownet/flow/domain/service/FlowOrchestratorService.java`
- [x] startFlow(): publish events, send first email
- [x] processApproval(): validate step, advance, publish events
- [x] processRejection(): mark rejected, cleanup, notify
- [x] processExpiration(): mark expired, cleanup, notify
- [x] repairFlow(): resume corrupted flows
- [x] FlowDeadlineService: @Scheduled cron for auto-expiration

### Task 3.4 — Email Service
**Files**: `src/backend/src/main/java/com/workflownet/flow/infrastructure/email/EmailSenderService.java`
- [x] JavaMailSender integration
- [x] Approval email with JWS link
- [x] Notification emails (start, rejection, completion)
- [x] Completion email with document attachment
- [x] Retry logic via @RetryableTopic

---

## Phase 4: Application Services & REST API

### Task 4.1 — DTOs & Mappers
**Files**: `src/backend/src/main/java/com/workflownet/flow/application/dto/*`, `flow/application/mapper/FlowMapper.java`
- [x] CreateFlowRequest, FlowResponse, ParticipantDto, DocumentDto, ActionRequest
- [x] FlowMapper for domain <-> DTO mapping

### Task 4.2 — Flow Service
**Files**: `src/backend/src/main/java/com/workflownet/flow/application/service/FlowService.java`
- [x] createFlow, getFlow, getFlowsByUser, getAllFlows, cancelFlow, getFlowStats

### Task 4.3 — Auth Service
**Files**: `src/backend/src/main/java/com/workflownet/auth/*`
- [x] AuthService: processOAuthLogin, getUserProfile, getCurrentUser
- [x] JwtTokenService: generateApprovalToken (JWS), validateApprovalToken
- [x] GitHubOAuthService: handle OAuth callback, fetch email from GitHub API
- [x] UserProfile DTO
- [x] JwtAuthenticationFilter (OncePerRequestFilter)

### Task 4.4 — Document Service
**Files**: `src/backend/src/main/java/com/workflownet/document/*`
- [x] DocumentService: upload, list, download, computeHash, cleanup
- [x] DocumentController: POST /upload, GET /flow/{flowId}, GET /{id}/download

### Task 4.5 — Audit Service
**Files**: `src/backend/src/main/java/com/workflownet/audit/*`
- [x] AuditService: logEvent (async), getAuditLogByFlow
- [x] AuditController: GET /audit/flow/{flowId}

### Task 4.6 — REST Controllers
**Files**: `src/backend/src/main/java/com/workflownet/**/presentation/controller/*`
- [x] FlowController: CRUD + approve/reject/cancel endpoints
- [x] AuthController: /auth/profile, /auth/github/callback, /auth/verify-token
- [x] AdminController: admin flows grid, reject, relaunch, stats, health
- [x] AuditController: audit log retrieval

---

## Phase 5: Frontend Application

### Task 5.1 — Angular Project Scaffolding
**Files**: `src/frontend/*`
- [x] Initialize Angular 22 project with routing
- [x] Install Tailwind CSS
- [x] Create proxy.conf.json for API proxy
- [x] Create environment files (dev/prod)
- [x] Core module structure (components, pages, services, models, guards, interceptors)

### Task 5.2 — Authentication Module
**Files**: `src/frontend/src/app/auth/*`
- [x] Login page with GitHub OAuth button
- [x] Auth guard for protected routes
- [x] Auth interceptor for API requests
- [x] Auth service with token management
- [x] User profile display

### Task 5.3 — Admin Dashboard
**Files**: `src/frontend/src/app/pages/admin/*`
- [x] KPI cards (total flows, by status, pending, expired)
- [x] Quick action buttons (create flow)

### Task 5.4 — Flow Management
**Files**: `src/frontend/src/app/pages/flows/*`
- [x] Create flow form (title, description, deadline, recipients)
- [x] Document upload component (with drag-and-drop)
- [x] Master grid with: search, sort, filter, pagination, URL state
- [x] Flow detail view
- [x] Approve/Reject actions

### Task 5.5 — Recipient View
**Files**: `src/frontend/src/app/pages/recipient/*`
- [x] Pending flows grid for recipient
- [x] Approve/reject flow action

---

## Phase 6: Infrastructure & Deployment

### Task 6.1 — Dockerfiles
**Files**: `src/backend/Dockerfile`, `src/frontend/Dockerfile`, `src/frontend/nginx.conf`
- [x] Multi-stage build for backend (gradle build → jre run)
- [x] Multi-stage build for frontend (node build → nginx serve)
- [x] Nginx config with API proxy

### Task 6.2 — Full Docker Compose
**Files**: `src/docker/docker-compose.yml`
- [x] All services: mongodb, kafka, zookeeper, backend, frontend, traefik, prometheus, grafana
- [x] Health checks, volumes, environment variables from .env

### Task 6.3 — Monitoring Stack
**Files**: `src/docker/prometheus/prometheus.yml`, `src/docker/grafana/provisioning/*`
- [x] Prometheus config with backend scrape target
- [x] Grafana datasource (Prometheus)
- [x] Grafana dashboard provider

### Task 6.4 — Traefik Configuration
**Files**: `src/docker/traefik/*`
- [x] Static config (entrypoints, ACME with Cloudflare)
- [x] Dynamic config for routing

### Task 6.5 — CI/CD & Scripts
**Files**: `.github/workflows/*`, `start-dev.bat`
- [x] GitHub Actions: build backend, build frontend, push to Docker Hub
- [x] start-dev.bat for local development

### Task 6.6 — Documentation
**Files**: `README.md`, `AGENTS.md`
- [x] Project overview, setup instructions, architecture diagram
- [x] Agent configuration and instructions
