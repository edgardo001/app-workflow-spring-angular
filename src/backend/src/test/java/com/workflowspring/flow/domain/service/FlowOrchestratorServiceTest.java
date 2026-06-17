package com.workflowspring.flow.domain.service;

import com.workflowspring.audit.AuditService;
import com.workflowspring.document.DocumentService;
import com.workflowspring.document.infrastructure.TempDocumentRepository;
import com.workflowspring.flow.domain.event.IdempotencyKey;
import com.workflowspring.flow.domain.model.*;
import com.workflowspring.flow.infrastructure.messaging.FlowEventPublisher;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import com.workflowspring.flow.infrastructure.persistence.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowOrchestratorServiceTest {

    @TempDir
    Path tempDir;

    @Mock private FlowRepository flowRepository;
    @Mock private FlowEventPublisher eventPublisher;
    @Mock private IdempotencyRepository idempotencyRepository;
    @Mock private AuditService auditService;
    @Mock private DocumentService documentService;
    @Mock private TempDocumentRepository tempDocumentRepository;

    private FlowOrchestratorService orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new FlowOrchestratorService(
                flowRepository, eventPublisher, idempotencyRepository,
                auditService, documentService, tempDocumentRepository);
    }

    private Flow createActiveFlowWithOneParticipant() throws IOException {
        Path tempFile = tempDir.resolve("doc1.pdf");
        Files.writeString(tempFile, "test content");
        Flow flow = new Flow("Test", "Desc", Instant.now().plusSeconds(86400), "creator");
        flow.addDocument(new DocumentMetadata("doc1", 100L, "text/plain", tempFile.toString()));
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        flow.setCurrentStep(0);
        flow.setStatus(FlowStatus.ACTIVE);
        return flow;
    }

    private Flow createActiveFlowWithTwoParticipants() throws IOException {
        Path tempFile = tempDir.resolve("doc1.pdf");
        Files.writeString(tempFile, "test content");
        Flow flow = new Flow("Test", "Desc", Instant.now().plusSeconds(86400), "creator");
        flow.addDocument(new DocumentMetadata("doc1", 100L, "text/plain", tempFile.toString()));
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        flow.addParticipant(new Participant("bob@test.com", "approver"));
        flow.setCurrentStep(0);
        flow.setStatus(FlowStatus.ACTIVE);
        return flow;
    }

    @Test
    void shouldStartFlowAndPublishEvents() throws IOException {
        Flow flow = createActiveFlowWithOneParticipant();
        when(flowRepository.save(any(Flow.class))).thenReturn(flow);

        orchestrator.startFlow(flow);

        verify(eventPublisher).publishFlowCreated(any());
        verify(eventPublisher).publishFlowStarted(any());
        verify(eventPublisher).publishEmailSend(any());
        verify(auditService).logEvent(eq(flow.getId()), anyString());
    }

    @Test
    void shouldApproveStepAndAdvance() throws IOException {
        Flow flow = createActiveFlowWithTwoParticipants();
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));
        when(flowRepository.save(any(Flow.class))).thenReturn(flow);
        when(idempotencyRepository.existsByKey("approval-flow1-0")).thenReturn(false);

        Flow result = orchestrator.processApproval("flow1", 0, "alice@test.com", "user1");

        assertEquals(FlowStatus.PENDING_APPROVAL, result.getStatus());
        assertEquals(1, result.getCurrentStep());
        verify(eventPublisher).publishDocumentApproved(any());
    }

    @Test
    void shouldCompleteOnLastApproval() throws IOException {
        Flow flow = createActiveFlowWithOneParticipant();
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));
        when(flowRepository.save(any(Flow.class))).thenReturn(flow);
        when(idempotencyRepository.existsByKey("approval-flow1-0")).thenReturn(false);

        Flow result = orchestrator.processApproval("flow1", 0, "alice@test.com", "user1");

        assertEquals(FlowStatus.COMPLETED, result.getStatus());
        verify(eventPublisher).publishFlowCompleted(any());
    }

    @Test
    void shouldSkipDuplicateApproval() throws IOException {
        Flow flow = createActiveFlowWithOneParticipant();
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));
        when(idempotencyRepository.existsByKey("approval-flow1-0")).thenReturn(true);

        orchestrator.processApproval("flow1", 0, "alice@test.com", "user1");

        verify(idempotencyRepository, never()).save(any());
        verify(eventPublisher, never()).publishDocumentApproved(any());
    }

    @Test
    void shouldRejectFlow() throws IOException {
        Flow flow = createActiveFlowWithOneParticipant();
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));
        when(flowRepository.save(any(Flow.class))).thenReturn(flow);
        when(idempotencyRepository.existsByKey("rejection-flow1-0")).thenReturn(false);

        Flow result = orchestrator.processRejection("flow1", 0, "alice@test.com", "user1", "Not needed");

        assertEquals(FlowStatus.REJECTED, result.getStatus());
        verify(eventPublisher).publishDocumentRejected(any());
        verify(tempDocumentRepository).deleteByFlowId("flow1");
    }

    @Test
    void shouldExpireFlow() throws IOException {
        Flow flow = createActiveFlowWithOneParticipant();
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));
        when(flowRepository.save(any(Flow.class))).thenReturn(flow);

        Flow result = orchestrator.processExpiration("flow1");

        assertEquals(FlowStatus.EXPIRED, result.getStatus());
        verify(eventPublisher).publishFlowExpired(any());
        verify(tempDocumentRepository).deleteByFlowId("flow1");
    }

    @Test
    void shouldRepairFlowByResendingEmail() throws IOException {
        Flow flow = createActiveFlowWithOneParticipant();
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));

        orchestrator.repairFlow("flow1");

        verify(eventPublisher).publishEmailSend(any());
        verify(auditService).logEvent(eq("flow1"), contains("REPAIR_EMAIL_RESENT"));
    }
}
