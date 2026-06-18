# Task Breakdown: Sequential Approval Flow & Pending List Fixes

## Phase 1: Backend DTOs, Mapping & Entities
- [x] **Task 1.1**: Update `CreateFlowRequest.java` to add `documentIds` field.
- [x] **Task 1.2**: Update `FlowResponse.java` and `ParticipantDto.java` with missing fields.
- [x] **Task 1.3**: Update `FlowMapper.java` to map all fields from domain entities, including calculating `isMyTurn` and setting participant status.

## Phase 2: Flow Creation & Orchestration Startup
- [x] **Task 2.1**: Update `FlowService.java` to associate documents, add participants, and call `FlowOrchestratorService.startFlow`.
- [x] **Task 2.2**: Update `FlowOrchestratorService.java` to inject `JwtTokenService` and `frontendUrl`, generate tokens on start/advance/reminder, set tokens in `Participant` instances, and format HTML mail bodies with the correct `http://localhost:4200/approve?token=...` link.

## Phase 3: Kafka Email Consumer & Resiliency
- [x] **Task 3.1**: Rewrite `EmailSenderService.java` to have a generic `sendEmail(EmailSendEvent event)` method that attaches files.
- [x] **Task 3.2**: Add `email.send` listener in `FlowEventConsumer.java` that invokes `EmailSenderService.sendEmail`.

## Phase 4: Token-less Approvals & Verification Endpoint
- [x] **Task 4.1**: Modify `FlowController.java` to make the token optional in `/approve` and `/reject` if the user is authenticated.
- [x] **Task 4.2**: Add `GET /api/flows/verify` endpoint in `FlowController.java` to resolve flow details using a JWS token.

## Phase 5: Backend Tests Update
- [x] **Task 5.1**: Update `FlowServiceTest` and `FlowOrchestratorServiceTest` with the new mocked dependencies and run them.

## Phase 6: Frontend App Updates
- [x] **Task 6.1**: Update types and endpoints in `flow.service.ts`.
- [x] **Task 6.2**: Integrate document uploading and proper request mapping in `new-flow.component.ts`.
- [x] **Task 6.3**: Fix `toSignal` reactive logic in `pending-flows.component.ts`.
- [x] **Task 6.4**: Create `ApproveFlowComponent` and register the `/approve` route in `app.routes.ts`.
