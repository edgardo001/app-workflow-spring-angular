# Implementation Plan: Sequential Approval Flow & Pending List Fixes

**Branch**: `002-approval-flow-fix` | **Spec**: [spec.md](spec.md)

## Summary
Implement all required fixes to restore full end-to-end functionality of the sequential approval workflow. This covers flow starting on creation, document upload integration, JWS link correction, optional JWS verification on authenticated requests, the frontend approval view, the frontend pending list reactive update, and the Kafka email consumer.

## Files to Modify

### Backend:
1. **`com.workflowspring.flow.application.dto.CreateFlowRequest`**:
   - Add `documentIds` field.
2. **`com.workflowspring.flow.application.dto.FlowResponse`**:
   - Add `ownerEmail`, `step`, `totalSteps`, `documents`, `isMyTurn`, `createdAt`.
3. **`com.workflowspring.flow.application.dto.ParticipantDto`**:
   - Add `name`, `stepOrder`, `status`, `approvedAt`.
4. **`com.workflowspring.flow.application.mapper.FlowMapper`**:
   - Update mapping to populate the new response and participant fields (including `isMyTurn` and `status`).
5. **`com.workflowspring.flow.application.service.FlowService`**:
   - Inject `DocumentService`, `TempDocumentRepository`, and `FlowOrchestratorService`.
   - Update `createFlow` to link documents/participants and trigger `flowOrchestratorService.startFlow`.
6. **`com.workflowspring.flow.domain.service.FlowOrchestratorService`**:
   - Inject `JwtTokenService` and `@Value("${app.frontend-url}") String frontendUrl`.
   - Update `startFlow`, `processApproval`, and `repairFlow` to generate JWS tokens, update participant fields, and format HTML mail bodies with JWS links.
7. **`com.workflowspring.flow.infrastructure.email.EmailSenderService`**:
   - Add a single `sendEmail(EmailSendEvent event)` method and remove publisher calls inside it.
8. **`com.workflowspring.flow.infrastructure.messaging.FlowEventConsumer`**:
   - Add listener for `email.send` topic that delegates to `emailSenderService.sendEmail`.
9. **`com.workflowspring.flow.presentation.controller.FlowController`**:
   - Make JWS token validation optional in `approve` and `reject` if the user is authenticated.
   - Add `GET /api/flows/verify` to fetch flow details by action token.

### Frontend:
1. **`src/frontend/src/app/services/flow.service.ts`**:
   - Update type interfaces `Flow`, `FlowParticipant`, `FlowDocument`, and `CreateFlowRequest` to match backend models.
   - Update `approveFlow` and `rejectFlow` to accept a JWS token parameter.
   - Add `verifyToken(token: string)` endpoint client method.
2. **`src/frontend/src/app/components/new-flow/new-flow.component.ts`**:
   - Perform sequential upload of files before calling `flowService.createFlow` and map properties correctly.
3. **`src/frontend/src/app/components/pending-flows/pending-flows.component.ts`**:
   - Fix reactive signal declaration by keeping `flows` as a signal and computing `flow`.
4. **`src/frontend/src/app/app.routes.ts`**:
   - Register route `/approve` to load `ApproveFlowComponent`.
5. **`src/frontend/src/app/components/approve-flow/approve-flow.component.ts`** (New):
   - Handle token parsing, loading details via `verifyToken`, and performing approval/rejection.

## Testing Strategy
1. **Unit Tests (Backend)**:
   - Run existing `FlowServiceTest` and `FlowOrchestratorServiceTest` and update mocks to include new dependencies.
2. **End-to-End Verification (Manual)**:
   - Run the development environment with `start-dev.bat`.
   - Create a flow with 2 participants and upload a test PDF.
   - Verify document is stored in the database.
   - Open greenmail or console logs to get the JWS link.
   - Verify approval advances the step.
   - Verify approval from Pending flows list works directly.
