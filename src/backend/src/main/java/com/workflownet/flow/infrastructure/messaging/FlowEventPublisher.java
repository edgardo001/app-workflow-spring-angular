package com.workflownet.flow.infrastructure.messaging;

import com.workflownet.flow.domain.event.DocumentApprovedEvent;
import com.workflownet.flow.domain.event.DocumentRejectedEvent;
import com.workflownet.flow.domain.event.FlowCompletedEvent;
import com.workflownet.flow.domain.event.FlowCreatedEvent;
import com.workflownet.flow.domain.event.FlowExpiredEvent;
import com.workflownet.flow.domain.event.FlowStartedEvent;
import com.workflownet.shared.event.EmailFailedEvent;
import com.workflownet.shared.event.EmailSendEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FlowEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishFlowCreated(FlowCreatedEvent event) {
        kafkaTemplate.send("flow.created", event.getFlowId(), event);
    }

    public void publishFlowStarted(FlowStartedEvent event) {
        kafkaTemplate.send("flow.started", event.getFlowId(), event);
    }

    public void publishDocumentApproved(DocumentApprovedEvent event) {
        kafkaTemplate.send("flow.document.approved", event.getFlowId(), event);
    }

    public void publishDocumentRejected(DocumentRejectedEvent event) {
        kafkaTemplate.send("flow.document.rejected", event.getFlowId(), event);
    }

    public void publishFlowExpired(FlowExpiredEvent event) {
        kafkaTemplate.send("flow.expired", event.getFlowId(), event);
    }

    public void publishFlowCompleted(FlowCompletedEvent event) {
        kafkaTemplate.send("flow.completed", event.getFlowId(), event);
    }

    public void publishEmailSend(EmailSendEvent event) {
        kafkaTemplate.send("email.send", event.getFlowId(), event);
    }

    public void publishEmailFailed(EmailFailedEvent event) {
        kafkaTemplate.send("email.failed", event.getFlowId(), event);
    }
}
