package com.workflowspring.flow.infrastructure.messaging;

import com.workflowspring.flow.domain.event.DocumentApprovedEvent;
import com.workflowspring.flow.domain.event.DocumentRejectedEvent;
import com.workflowspring.flow.domain.event.FlowCreatedEvent;
import com.workflowspring.flow.domain.event.FlowExpiredEvent;
import com.workflowspring.flow.infrastructure.email.EmailSenderService;
import com.workflowspring.flow.infrastructure.persistence.IdempotencyRepository;
import com.workflowspring.shared.event.EmailFailedEvent;
import com.workflowspring.shared.event.EmailSendEvent;
import com.workflowspring.flow.domain.event.IdempotencyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class FlowEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FlowEventConsumer.class);

    private final IdempotencyRepository idempotencyRepository;
    private final EmailSenderService emailSenderService;

    public FlowEventConsumer(IdempotencyRepository idempotencyRepository,
                             EmailSenderService emailSenderService) {
        this.idempotencyRepository = idempotencyRepository;
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "email.send", groupId = "email-group")
    public void consumeEmailSend(@Payload EmailSendEvent event) {
        log.info("Consuming EmailSendEvent to {}", event.getTo());
        emailSenderService.sendEmail(event);
    }

    private boolean isProcessed(String eventId) {
        if (idempotencyRepository.existsByKey(eventId)) {
            log.warn("Event {} already processed, skipping", eventId);
            return true;
        }
        idempotencyRepository.insert(new IdempotencyKey(eventId));
        return false;
    }

    @KafkaListener(topics = "flow.created", groupId = "flow-group")
    public void consumeFlowCreated(@Payload FlowCreatedEvent event) {
        log.info("Flow created: id={}, title={}, createdBy={}", event.getFlowId(), event.getTitle(), event.getCreatedBy());
    }

    @KafkaListener(topics = "flow.document.approved", groupId = "flow-group")
    public void consumeDocumentApproved(@Payload DocumentApprovedEvent event) {
        if (isProcessed(event.getEventId())) return;
        log.info("Document approved for flow: {}, step: {}, by: {}", event.getFlowId(), event.getStepNumber(), event.getParticipantEmail());
    }

    @KafkaListener(topics = "flow.document.rejected", groupId = "flow-group")
    public void consumeDocumentRejected(@Payload DocumentRejectedEvent event) {
        if (isProcessed(event.getEventId())) return;
        log.info("Document rejected for flow: {}, step: {}, by: {}, reason: {}", event.getFlowId(), event.getStepNumber(), event.getParticipantEmail(), event.getReason());
    }

    @KafkaListener(topics = "flow.expired", groupId = "flow-group")
    public void consumeFlowExpired(@Payload FlowExpiredEvent event) {
        if (isProcessed(event.getEventId())) return;
        log.info("Flow expired: id={}", event.getFlowId());
    }

    @Retryable(
        retryFor = {Exception.class},
        backoff = @Backoff(delay = 5000, multiplier = 3.0),
        maxAttempts = 5
    )
    @KafkaListener(topics = "email.failed", groupId = "email-group")
    public void consumeEmailFailed(@Payload EmailFailedEvent event) {
        log.error("Email failed for flow: {}, to: {}, subject: {}, error: {}, attempt: {}",
            event.getFlowId(), event.getTo(), event.getSubject(), event.getErrorMessage(), event.getAttemptNumber());
    }
}
