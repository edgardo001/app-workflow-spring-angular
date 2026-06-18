# Feature Specification: Document Workflow Application

**Feature**: `001-workflow-app`

**Created**: 2026-06-15

**Status**: Draft

**Input**: User description from planInicial.ia.md — a document sequential approval workflow system

## User Scenarios & Testing

### User Story 1 - Admin Creates and Manages Document Flow (Priority: P1)

As an administrator, I want to create a document flow by uploading documents (up to 5, max 2MB each), adding sequential recipient emails, setting a deadline, and monitoring progress via a KPI dashboard, so that I can manage document approval workflows.

**Why this priority**: This is the core use case — without flow creation there is no system.

**Independent Test**: Can be fully tested by: admin logs in via GitHub OAuth → creates flow with documents and recipients → flow appears in grid with correct status → documents are stored temporarily.

**Acceptance Scenarios**:

1. **Given** an authenticated admin user, **When** they upload a document larger than 2MB, **Then** the system rejects it with an error message.
2. **Given** an authenticated admin user, **When** they try to upload more than 5 documents to a flow, **Then** the system rejects with max count error.
3. **Given** a created flow with recipients, **When** the admin views the grid, **Then** they see search, sort, filter, pagination with state persisted in URL.
4. **Given** an active flow, **When** the admin rejects it with a reason, **Then** all participants receive a rejection email with the reason.

---

### User Story 2 - Recipient Approves Document via Email (Priority: P1)

As a designated recipient, I want to receive an email with a secure approval link, click it, and approve the document so that the workflow advances to the next recipient.

**Why this priority**: The sequential approval is the primary business value — without approval the flow cannot progress.

**Independent Test**: Can be fully tested by: recipient receives email → clicks JWS link → system validates token → document advances to next step.

**Acceptance Scenarios**:

1. **Given** a flow at step N, **When** recipient N clicks the approval link, **Then** the document advances to step N+1 and the next recipient receives their email.
2. **Given** a flow at the final step, **When** the last recipient approves, **Then** the flow completes, document hash is stored, and all participants receive a completion email with the document attached.
3. **Given** a recipient who is not logged in, **When** they click the approval link, **Then** they are prompted to log in via GitHub OAuth before the action is processed.

---

### User Story 3 - Recipient Rejects Document (Priority: P2)

As a designated recipient, I want to reject a document with a reason so that the flow stops and all participants are notified.

**Why this priority**: Rejection handling is essential for workflow integrity but can be developed after basic approval flow.

**Independent Test**: Can be fully tested by: recipient clicks rejection link → enters reason → flow marks as rejected → all participants notified.

**Acceptance Scenarios**:

1. **Given** an active flow, **When** a recipient rejects with reason "Missing signature", **Then** the flow status changes to REJECTED and all participants receive a rejection email with the reason.
2. **Given** a rejected flow, **When** the admin views it, **Then** they can see the rejection reason and relaunch the flow if desired.

---

### User Story 4 - Automatic Flow Expiration (Priority: P3)

As a system, I want to automatically expire flows that exceed their deadline plus a grace period (configurable, default 3 days) so that stale workflows are cleaned up.

**Why this priority**: Important for system hygiene but can be implemented after core functionality.

**Independent Test**: Can be fully tested by: setting a deadline in the past → running the scheduled check → flow becomes EXPIRED → participants notified.

**Acceptance Scenarios**:

1. **Given** a flow past its deadline + grace period, **When** the daily cron job runs, **Then** the flow is marked EXPIRED.
2. **Given** an expired flow, **When** a recipient clicks their approval link, **Then** they receive an error that the document has expired.

---

### User Story 5 - Admin Dashboard with KPIs (Priority: P3)

As an admin, I want to see a KPI dashboard showing flow statistics (total, by status, pending, expired) so that I can monitor system health.

**Why this priority**: Adds visibility but not critical for core workflow.

**Independent Test**: Can be fully tested by: navigating to dashboard → seeing correct counts for each flow status.

**Acceptance Scenarios**:

1. **Given** flows in various states, **When** the admin visits the dashboard, **Then** they see correct counts for ACTIVE, COMPLETED, REJECTED, EXPIRED, and CANCELLED flows.

---

### Edge Cases

- What happens when a recipient clicks an approval link for an already-completed flow?
- How does the system handle a recipient whose email doesn't match their GitHub OAuth email?
- What happens when the email server is down during flow creation?
- How does the system recover from a Kafka broker failure mid-flow?
- What happens if a document upload is interrupted mid-transfer?

## Requirements

### Functional Requirements

- **FR-001**: System MUST authenticate users via GitHub OAuth
- **FR-002**: System MUST allow admin to create flows with title, description, and deadline
- **FR-003**: System MUST allow uploading 1-5 documents per flow, each max 2MB
- **FR-004**: System MUST validate document size and count server-side
- **FR-005**: System MUST store documents temporarily with TTL-based cleanup
- **FR-006**: System MUST allow adding sequential recipient emails to a flow
- **FR-007**: System MUST generate a unique JWS token for each approval step
- **FR-008**: System MUST send email notifications for flow start, approval request, rejection, completion, and expiration
- **FR-009**: System MUST validate JWS tokens on each approval/rejection action
- **FR-010**: System MUST advance the flow to the next recipient upon approval
- **FR-011**: System MUST mark flow as COMPLETED when last recipient approves
- **FR-012**: System MUST store document hash (SHA-256) upon flow completion
- **FR-013**: System MUST clean up temporary documents upon flow completion or rejection
- **FR-014**: System MUST mark flow as REJECTED when a recipient rejects with a reason
- **FR-015**: System MUST notify all participants when flow is rejected
- **FR-016**: System MUST auto-expire flows past deadline + configurable grace period
- **FR-017**: System MUST log every action in append-only flow_audit_log
- **FR-018**: System MUST provide a grid for admin with search, sort, filter, pagination (state in URL)
- **FR-019**: System MUST provide a grid for recipients with their pending flows
- **FR-020**: System MUST allow admin to reject or relaunch existing flows
- **FR-021**: System MUST implement idempotent consumers for Kafka events
- **FR-022**: System MUST implement email retry with backoff and DLQ (max 5 attempts)
- **FR-023**: System MUST expose healthcheck and Prometheus metrics endpoints
- **FR-024**: System SHOULD provide admin KPI dashboard
- **FR-025**: System MUST use OpenAPI/Swagger UI enabled in production

### Key Entities

- **Flow**: The aggregate root representing a document approval workflow. Contains status, participants, documents, deadline, and current step.
- **Document**: Metadata about an uploaded document including filename, size, temp path, and computed hash (after completion).
- **Participant**: A recipient in the approval sequence. Has email, name, step order, JWS token, approval/rejection status.
- **FlowAuditLog**: Append-only record of every action in the system. Contains flowId, action, userId, timestamp, metadata.
- **TempDocument**: The actual file bytes stored temporarily with TTL index. Deleted after flow completes or is rejected.
- **IdempotencyKey**: Unique key tracking processed Kafka events to ensure exactly-once processing.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Admin can complete flow creation with documents and recipients in under 5 minutes
- **SC-002**: Recipients can approve a document in under 2 clicks from email
- **SC-003**: System handles 100 concurrent flows without degradation
- **SC-004**: Email notification delivery time under 30 seconds (excluding external delays)
- **SC-005**: Audit log maintains complete traceability for all flows (zero missing events)

## Assumptions

- Users have stable internet connectivity
- Email delivery is handled by an external SMTP server
- GitHub OAuth is available and accessible
- MongoDB replica set is available for production (single node for dev)
- Kafka cluster is available (single broker for dev)
- Document types are typical office formats (PDF, DOCX, images)
- Mobile support is out of scope for v1
- The system will be deployed on Linux VPS with Docker
