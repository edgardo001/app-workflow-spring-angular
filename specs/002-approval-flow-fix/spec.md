# Feature Specification: Sequential Approval Flow & Pending List Fixes

**Feature**: `002-approval-flow-fix`
**Created**: 2026-06-18
**Status**: Approved

## Problem Statement
The current implementation of the sequential approval workflow has several critical bugs:
1. **Broken Email Approval Link**: The email link is hardcoded to a production domain and uses a route `/approve` which does not exist in the Angular router.
2. **Incomplete Flow Creation**: When creating a flow, selected documents are not uploaded to the backend, and participants are not added or saved in the database. The orchestrator's `startFlow` is never invoked.
3. **Empty Pending list**: The frontend "Pending" page uses `toSignal(loadPendingFlows())()` statically, meaning it evaluates once to `[]` and never updates when data arrives.
4. **Missing Kafka Email Consumer**: Although the orchestrator publishes `EmailSendEvent` to Kafka, there is no consumer listening to the `email.send` topic, meaning emails are never sent.
5. **Strict JWS Requirement on Web App**: Logged-in users cannot approve/reject directly from the Pending page without providing a JWS token.

## Requirements

### Functional Requirements

- **FR-001**: Backend must allow approval/rejection using the security context principal's email as an alternative to JWS tokens when the user is authenticated.
- **FR-002**: Frontend must implement an `/approve` route that displays the flow details and allows the recipient to approve/reject the step.
- **FR-003**: The approval email link must construct the URL using the configured frontend base URL (`app.frontend-url`).
- **FR-004**: Frontend `NewFlowComponent` must upload selected documents using `DocumentService.upload(file)` and pass their returned UUIDs in the creation request.
- **FR-005**: Backend `FlowService.createFlow(...)` must associate the `documentIds` with the flow, update `TempDocument` instances with the `flowId`, add the participants in sequential order, and start the flow.
- **FR-006**: Backend must have an `email.send` Kafka listener that processes email sending through `EmailSenderService.sendEmail(...)`.
- **FR-007**: Frontend `PendingFlowsComponent` must use reactive signals (using `computed` on `toSignal()`) to load and display pending flows dynamically.

### REST API Updates

#### GET `/api/flows/verify`
- **Query Parameter**: `token` (String, required)
- **Description**: Validates the approval JWS token, extracts the flow ID and current step, and returns the full `FlowResponse` details.
- **Response**: `FlowResponse`

#### POST `/api/flows/{id}/approve` & POST `/api/flows/{id}/reject`
- **Request Body**: `ActionRequest`
- **Description**: If `token` is missing or empty, fallback to the authenticated user's email principal. If `token` is present, validate it.

## User Experience
- **Clicking Email Link**: A recipient clicks the email link -> Lands on the frontend `/approve?token=...` page. The page retrieves flow details and displays them. The user checks a box and clicks "Approve" (or inputs a reason and clicks "Reject").
- **Pending page**: A logged-in recipient goes to the "Pending" page -> Sees the list of flows pending their action. They can check the box and approve/reject directly, without clicking any email link.
