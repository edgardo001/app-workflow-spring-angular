package com.workflowspring.flow.infrastructure.messaging;

import com.workflowspring.flow.domain.event.DocumentApprovedEvent;
import com.workflowspring.flow.domain.event.DocumentRejectedEvent;
import com.workflowspring.flow.domain.event.FlowCreatedEvent;
import com.workflowspring.flow.domain.event.FlowExpiredEvent;
import com.workflowspring.flow.infrastructure.persistence.IdempotencyRepository;
import com.workflowspring.flow.domain.event.IdempotencyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class FlowEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FlowEventConsumer.class);

    private final IdempotencyRepository idempotencyRepository;

    public FlowEventConsumer(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
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
}
