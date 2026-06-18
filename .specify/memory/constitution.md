<!--
Sync Impact Report:
  Version change: 0.0.0 → 1.0.0 (initial constitution ratification)
  Added principles: Clean Architecture & Vertical Slices, Event-Driven & SAGA, TDD-First, Security-First, Observability
  Templates updated: ✅ spec-template.md, plan-template.md, tasks-template.md
-->

# Workflow Net Constitution

## Core Principles

### I. Clean Architecture & Vertical Slices
Every feature is organized as a vertical slice (domain → application → infrastructure → presentation), not by technical layers. Dependencies point inward: infrastructure depends on application, which depends on domain. Domain models are plain POJOs with no framework annotations. This ensures features can be developed, tested, and deployed independently.

### II. Event-Driven Orchestration (SAGA)
Business flows use the SAGA orchestration pattern via Kafka. A single `FlowOrchestratorService` centralizes the workflow sequence. Every state transition publishes a domain event. Consumers MUST be idempotent: each event carries a unique `eventId` (UUID), and consumers check a MongoDB collection with a unique index before processing to guarantee exactly-once semantics.

### III. TDD-First (NON-NEGOTIABLE)
Tests are written BEFORE implementation code. The cycle is: Red (test fails) → Green (minimal implementation) → Refactor. Every vertical slice must have unit tests for domain logic, integration tests for persistence, and contract tests for API endpoints. Tests are the specification of correctness.

### IV. Security-First
ALL user inputs MUST be validated server-side. Authentication uses GitHub OAuth. Action tokens are JWS (JSON Web Signature) signed by the backend — never plain IDs. To prevent cross-user impersonation and unauthorized signing, action endpoints verify that the authenticated session email matches the email claim of the JWS token. The audit log (`flow_audit_log`) is append-only: no UPDATE or DELETE allowed. Secrets live exclusively in `.env` (never committed). Admin credentials and OAuth secrets are configured via environment variables.

### V. Observability & Traceability
Every action generates an audit log entry in `flow_audit_log`. The system exposes health (`/actuator/health`) and metrics (`/actuator/prometheus`) endpoints. Failed email deliveries go to a Kafka DLQ. Failed/completed email bodies are processed using dynamic Thymeleaf templates before sending. Flows past deadline + grace period are automatically expired via `@Scheduled` job. Grafana dashboards visualize KPI metrics from Prometheus.

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Backend | Spring Boot (Kotlin) | 3.4.2 |
| JDK | OpenJDK | 17 |
| Templating | Thymeleaf | — |
| Frontend | Angular | 22 |
| CSS | Tailwind CSS | — |
| Database | MongoDB | 8 |
| Messaging | Apache Kafka | 4.3.0 |
| Build (Backend) | Gradle (Kotlin DSL) | — |
| Containers | Docker + Compose | — |
| Proxy | Traefik | — |
| Monitoring | Grafana + Prometheus | — |

## Development Workflow

1. Feature specification is written and reviewed before any code
2. Implementation follows SDD: constitution → specify → plan → tasks → implement
3. Tests must pass before code is committed
4. All work on feature branches; never commit directly to main
5. Pull requests require review before merging
6. Architecture Decision Records (ADRs) must accompany significant decisions

## Governance

This constitution is the foundational governance document for Workflow Net. All principles are mandatory unless explicitly marked as "SHOULD" (recommended) vs "MUST" (required). Amendments must be documented, approved via PR, and include a migration plan. Complexity must be justified — prefer simple solutions over patterns-heavy abstractions.

**Version**: 1.0.0 | **Ratified**: 2026-06-15 | **Last Amended**: 2026-06-15
