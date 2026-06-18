<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
<!-- SPECKIT END -->

# SpecKit / Spec-Driven Development

This project uses **SpecKit** for Spec-Driven Development (SDD).

Available commands:
- `/speckit.constitution` — Load or initialize the project constitution
- `/speckit.specify` — Create or refine specifications
- `/speckit.plan` — Generate a plan from specifications
- `/speckit.tasks` — Break plans into actionable tasks
- `/speckit.implement` — Implement tasks

| Item | Location |
|------|----------|
| Specs directory | `specs/` |
| Constitution | `.specify/memory/constitution.md` |

---

# Project Overview

**app-workflow-net-angular** is a document sequential approval workflow system.

- Users upload documents and add sequential recipients
- Each recipient receives an email containing a JWS token to approve or reject
- Documents are stored temporarily; only hash + metadata remains after the flow completes
- Admin dashboard with KPIs, master grid with search/sort/filter/pagination (state persisted in URL)
- Recipients see pending flows and can approve/reject via token links

---

# Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.4.2, JDK 17, Gradle (Kotlin DSL) |
| Frontend | Angular 22, Tailwind CSS |
| Database | MongoDB 8 |
| Messaging | Apache Kafka 4.3.0 |
| Authentication | GitHub OAuth + JWS tokens |
| Containers | Docker + Docker Compose |
| Proxy | Traefik |
| Monitoring | Grafana + Prometheus |

---

# Architecture

- **Vertical Slice + Clean Architecture** — organized by feature: `flow`, `auth`, `audit`, `document`, `admin`
- **Event-Driven + SAGA orchestrated** by `FlowOrchestratorService`
- **Idempotent consumers** — unique keys in MongoDB prevent duplicate processing
- **Append-only audit log** (`flow_audit_log`) — immutable history of all flow events
- **TTL indexes** — auto-cleanup for temporary documents and expired tokens

---

# Key Files

| Path | Description |
|------|-------------|
| `src/backend/src/main/java/com/workflownet/` | Backend source code |
| `src/frontend/src/app/` | Frontend source code |
| `src/docker/docker-compose.yml` | Docker Compose orchestration |
| `OpenDesignBase/index.html` | App shell design |
| `OpenDesignBase/landing.html` | Landing page design |

---

# Git Workflow

- Work in **development branches / worktrees** — never commit directly to `main`
- Use **Pull Requests** for all changes
- **TDD** — write tests before implementation
- **SpecKit SDD** for all planning and task breakdowns

---

# Shell Commands

- `start-dev.bat` — Run locally (Docker for MongoDB + Kafka, local for backend + frontend)
- Backend: `./gradlew bootRun` (or `gradlew.bat bootRun` on Windows)
- Frontend: `ng serve --proxy-config proxy.conf.json`
- Docker Compose: `docker-compose -f src/docker/docker-compose.yml --env-file .env up`

---

# Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **SAGA Orchestrated vs Choreographed** | `FlowOrchestratorService` is easier to debug and maps cleanly to the sequential approval flow |
| **Vertical Slices vs Layered** | Features are independently testable with no cross-contamination between slices |
| **Idempotency** | `approval-{flowId}-{step}` unique key prevents double processing |
| **JWS Tokens** | Stateless action tokens contain `flowId`, `email`, `stepNumber` — validated by backend |
| **TTL Indexes** | Auto-cleanup of expired tokens and temporary documents with no cron jobs needed |
| **Strict Email Matching** | Verification that the authenticated user's email matches the JWS token's email claim prevents cross-user signature impersonation. |
