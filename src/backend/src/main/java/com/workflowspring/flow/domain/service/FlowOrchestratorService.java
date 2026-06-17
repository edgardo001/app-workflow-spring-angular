package com.workflowspring.flow.domain.service;

import com.workflowspring.audit.AuditService;
import com.workflowspring.document.DocumentService;
import com.workflowspring.document.infrastructure.TempDocumentRepository;
import com.workflowspring.flow.domain.event.DocumentApprovedEvent;
import com.workflowspring.flow.domain.event.DocumentRejectedEvent;
import com.workflowspring.flow.domain.event.FlowCompletedEvent;
import com.workflowspring.flow.domain.event.FlowCreatedEvent;
import com.workflowspring.flow.domain.event.FlowExpiredEvent;
import com.workflowspring.flow.domain.event.FlowStartedEvent;
import com.workflowspring.flow.domain.event.IdempotencyKey;
import com.workflowspring.flow.domain.model.DocumentMetadata;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import com.workflowspring.flow.domain.model.Participant;
import com.workflowspring.flow.infrastructure.messaging.FlowEventPublisher;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import com.workflowspring.flow.infrastructure.persistence.IdempotencyRepository;
import com.workflowspring.shared.event.EmailSendEvent;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlowOrchestratorService {

    private final FlowRepository flowRepository;
    private final FlowEventPublisher eventPublisher;
    private final IdempotencyRepository idempotencyRepository;
    private final AuditService auditService;
    private final DocumentService documentService;
    private final TempDocumentRepository tempDocumentRepository;

    public FlowOrchestratorService(FlowRepository flowRepository,
                                   FlowEventPublisher eventPublisher,
                                   IdempotencyRepository idempotencyRepository,
                                   AuditService auditService,
                                   DocumentService documentService,
                                   TempDocumentRepository tempDocumentRepository) {
        this.flowRepository = flowRepository;
        this.eventPublisher = eventPublisher;
        this.idempotencyRepository = idempotencyRepository;
        this.auditService = auditService;
        this.documentService = documentService;
        this.tempDocumentRepository = tempDocumentRepository;
    }

    public void startFlow(Flow flow) {
        flow.setStatus(FlowStatus.ACTIVE);
        flow.setCurrentStep(0);
        flowRepository.save(flow);

        FlowCreatedEvent createdEvent = new FlowCreatedEvent(flow.getId(), flow.getTitle(), flow.getCreatedBy());
        eventPublisher.publishFlowCreated(createdEvent);

        Participant first = flow.getParticipants().get(0);
        FlowStartedEvent startedEvent = new FlowStartedEvent(flow.getId(), 0, first.getEmail());
        eventPublisher.publishFlowStarted(startedEvent);

        EmailSendEvent emailEvent = new EmailSendEvent(
                flow.getId(),
                first.getEmail(),
                "Approval Required: " + flow.getTitle(),
                "Please review and approve the flow: " + flow.getTitle(),
                List.of(),
                0);
        eventPublisher.publishEmailSend(emailEvent);

        auditService.logEvent(flow.getId(), "FLOW_STARTED by " + flow.getCreatedBy());
    }

    public Flow processApproval(String flowId, int stepNumber, String participantEmail, String userId) {
        String idempotencyKey = "approval-" + flowId + "-" + stepNumber;
        if (idempotencyRepository.existsByKey(idempotencyKey)) {
            return flowRepository.findById(flowId).orElseThrow();
        }
        idempotencyRepository.save(new IdempotencyKey(idempotencyKey));

        Flow flow = flowRepository.findById(flowId).orElseThrow();
        if (flow.getCurrentStep() != stepNumber) {
            throw new IllegalStateException("Current step " + flow.getCurrentStep()
                    + " does not match expected step " + stepNumber);
        }

        flow.approve(participantEmail);

        if (flow.getStatus() == FlowStatus.COMPLETED) {
            List<String> hashes = new ArrayList<>();
            for (DocumentMetadata doc : flow.getDocuments()) {
                String hash = computeSha256(doc.getTempPath());
                hashes.add(hash);
                doc.setHash(hash);
            }
            tempDocumentRepository.deleteByFlowId(flowId);

            flowRepository.save(flow);
            eventPublisher.publishDocumentApproved(new DocumentApprovedEvent(flowId, stepNumber, participantEmail, userId));
            eventPublisher.publishFlowCompleted(new FlowCompletedEvent(flowId, hashes));

            List<String> participantEmails = flow.getParticipants().stream()
                    .map(Participant::getEmail)
                    .collect(Collectors.toList());
            EmailSendEvent emailEvent = new EmailSendEvent(
                    flowId, String.join(",", participantEmails),
                    "Flow Completed: " + flow.getTitle(),
                    "All approvals have been obtained.",
                    List.of(), stepNumber);
            eventPublisher.publishEmailSend(emailEvent);

            auditService.logEvent(flowId, "FLOW_COMPLETED last step " + stepNumber);
        } else {
            flowRepository.save(flow);
            eventPublisher.publishDocumentApproved(new DocumentApprovedEvent(flowId, stepNumber, participantEmail, userId));

            int nextStep = flow.getCurrentStep();
            Participant next = flow.getParticipants().get(nextStep);
            EmailSendEvent emailEvent = new EmailSendEvent(
                    flowId,
                    next.getEmail(),
                    "Approval Required: " + flow.getTitle(),
                    "Please review and approve the next step.",
                    List.of(),
                    nextStep);
            eventPublisher.publishEmailSend(emailEvent);

            auditService.logEvent(flowId, "STEP_" + stepNumber + "_APPROVED by " + participantEmail);
        }

        return flow;
    }

    public Flow processRejection(String flowId, int stepNumber, String participantEmail,
                                 String userId, String reason) {
        String idempotencyKey = "rejection-" + flowId + "-" + stepNumber;
        if (idempotencyRepository.existsByKey(idempotencyKey)) {
            return flowRepository.findById(flowId).orElseThrow();
        }
        idempotencyRepository.save(new IdempotencyKey(idempotencyKey));

        Flow flow = flowRepository.findById(flowId).orElseThrow();
        flow.reject(participantEmail, reason);

        flowRepository.save(flow);
        eventPublisher.publishDocumentRejected(new DocumentRejectedEvent(flowId, stepNumber, participantEmail, userId, reason));

        tempDocumentRepository.deleteByFlowId(flowId);

        List<String> participantEmails = flow.getParticipants().stream()
                .map(Participant::getEmail)
                .collect(Collectors.toList());
        EmailSendEvent emailEvent = new EmailSendEvent(
                flowId,
                String.join(",", participantEmails),
                "Flow Rejected: " + flow.getTitle(),
                "The flow has been rejected. Reason: " + reason,
                List.of(),
                stepNumber);
        eventPublisher.publishEmailSend(emailEvent);

        auditService.logEvent(flowId, "FLOW_REJECTED at step " + stepNumber + " by " + participantEmail);
        return flow;
    }

    public Flow processExpiration(String flowId) {
        Flow flow = flowRepository.findById(flowId).orElseThrow();
        flow.expire();

        flowRepository.save(flow);
        eventPublisher.publishFlowExpired(new FlowExpiredEvent(flowId));

        tempDocumentRepository.deleteByFlowId(flowId);

        List<String> participantEmails = flow.getParticipants().stream()
                .map(Participant::getEmail)
                .collect(Collectors.toList());
        EmailSendEvent emailEvent = new EmailSendEvent(
                flowId,
                String.join(",", participantEmails),
                "Flow Expired: " + flow.getTitle(),
                "The approval deadline has passed.",
                List.of(),
                flow.getCurrentStep());
        eventPublisher.publishEmailSend(emailEvent);

        auditService.logEvent(flowId, "FLOW_EXPIRED");
        return flow;
    }

    public Flow repairFlow(String flowId) {
        Flow flow = flowRepository.findById(flowId).orElseThrow();
        FlowStatus status = flow.getStatus();

        if (status == FlowStatus.ACTIVE || status == FlowStatus.PENDING_APPROVAL) {
            int step = flow.getCurrentStep();
            Participant current = flow.getParticipants().get(step);

            EmailSendEvent emailEvent = new EmailSendEvent(
                    flowId,
                    current.getEmail(),
                    "Reminder: Approval Required - " + flow.getTitle(),
                    "This is a reminder to review and approve the flow.",
                    List.of(),
                    step);
            eventPublisher.publishEmailSend(emailEvent);

            auditService.logEvent(flowId, "REPAIR_EMAIL_RESENT for step " + step);
        } else {
            auditService.logEvent(flowId, "REPAIR_SKIPPED flow status is " + status);
        }

        return flow;
    }

    private String computeSha256(String filePath) {
        try (InputStream is = Files.newInputStream(Path.of(filePath))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256 for " + filePath, e);
        }
    }
}
