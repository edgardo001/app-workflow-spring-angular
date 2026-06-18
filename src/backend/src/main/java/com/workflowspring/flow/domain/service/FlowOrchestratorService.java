package com.workflowspring.flow.domain.service;

import com.workflowspring.audit.AuditService;
import com.workflowspring.auth.JwtTokenService;
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
import com.workflowspring.flow.infrastructure.email.EmailSenderService;
import com.workflowspring.flow.infrastructure.messaging.FlowEventPublisher;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import com.workflowspring.flow.infrastructure.persistence.IdempotencyRepository;
import org.springframework.stereotype.Service;

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
    private final EmailSenderService emailSenderService;
    private final JwtTokenService jwtTokenService;

    public FlowOrchestratorService(FlowRepository flowRepository,
                                   FlowEventPublisher eventPublisher,
                                   IdempotencyRepository idempotencyRepository,
                                   AuditService auditService,
                                   DocumentService documentService,
                                   TempDocumentRepository tempDocumentRepository,
                                   EmailSenderService emailSenderService,
                                   JwtTokenService jwtTokenService) {
        this.flowRepository = flowRepository;
        this.eventPublisher = eventPublisher;
        this.idempotencyRepository = idempotencyRepository;
        this.auditService = auditService;
        this.documentService = documentService;
        this.tempDocumentRepository = tempDocumentRepository;
        this.emailSenderService = emailSenderService;
        this.jwtTokenService = jwtTokenService;
    }

    public void startFlow(Flow flow) {
        if (flow.getParticipants().isEmpty()) {
            throw new IllegalStateException("Cannot start a flow without participants");
        }

        flow.setStatus(FlowStatus.ACTIVE);
        flow.setCurrentStep(0);
        flowRepository.save(flow);

        eventPublisher.publishFlowCreated(new FlowCreatedEvent(flow.getId(), flow.getTitle(), flow.getCreatedBy()));

        Participant first = flow.getParticipants().get(0);
        eventPublisher.publishFlowStarted(new FlowStartedEvent(flow.getId(), 0, first.getEmail()));

        String token = jwtTokenService.generateApprovalToken(flow.getId(), first.getEmail());
        List<String> docNames = flow.getDocuments().stream()
                .map(DocumentMetadata::getFileName)
                .collect(Collectors.toList());

        emailSenderService.sendApprovalEmail(
                first.getEmail(),
                flow.getTitle(),
                token,
                flow.getId(),
                0,
                flow.getParticipants().size(),
                flow.getDescription(),
                docNames);

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
            emailSenderService.sendCompletionEmail(participantEmails, flow.getTitle(), List.of(), List.of());

            auditService.logEvent(flowId, "FLOW_COMPLETED last step " + stepNumber);
        } else {
            flowRepository.save(flow);
            eventPublisher.publishDocumentApproved(new DocumentApprovedEvent(flowId, stepNumber, participantEmail, userId));

            int nextStep = flow.getCurrentStep();
            Participant next = flow.getParticipants().get(nextStep);

            String token = jwtTokenService.generateApprovalToken(flowId, next.getEmail());
            List<String> docNames = flow.getDocuments().stream()
                    .map(DocumentMetadata::getFileName)
                    .collect(Collectors.toList());

            emailSenderService.sendApprovalEmail(
                    next.getEmail(),
                    flow.getTitle(),
                    token,
                    flowId,
                    nextStep,
                    flow.getParticipants().size(),
                    flow.getDescription(),
                    docNames);

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
        emailSenderService.sendRejectionNotification(participantEmails, flow.getTitle(), reason);

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
        emailSenderService.sendExpirationNotification(participantEmails, flow.getTitle());

        auditService.logEvent(flowId, "FLOW_EXPIRED");
        return flow;
    }

    public Flow repairFlow(String flowId) {
        Flow flow = flowRepository.findById(flowId).orElseThrow();
        FlowStatus status = flow.getStatus();

        if (status == FlowStatus.ACTIVE || status == FlowStatus.PENDING_APPROVAL) {
            int step = flow.getCurrentStep();
            Participant current = flow.getParticipants().get(step);

            String token = jwtTokenService.generateApprovalToken(flowId, current.getEmail());
            List<String> docNames = flow.getDocuments().stream()
                    .map(DocumentMetadata::getFileName)
                    .collect(Collectors.toList());

            emailSenderService.sendApprovalEmail(
                    current.getEmail(),
                    "Recordatorio: " + flow.getTitle(),
                    token,
                    flowId,
                    step,
                    flow.getParticipants().size(),
                    flow.getDescription(),
                    docNames);

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
