package com.workflownet.flow.domain.event;

import java.time.Instant;
import java.util.UUID;

public class DocumentRejectedEvent implements FlowEvent {

    private String eventId;
    private String flowId;
    private Instant timestamp;
    private int stepNumber;
    private String participantEmail;
    private String userId;
    private String reason;

    public DocumentRejectedEvent() {
    }

    public DocumentRejectedEvent(String flowId, int stepNumber, String participantEmail, String userId, String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.timestamp = Instant.now();
        this.stepNumber = stepNumber;
        this.participantEmail = participantEmail;
        this.userId = userId;
        this.reason = reason;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
