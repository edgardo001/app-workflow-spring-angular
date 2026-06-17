package com.workflownet.flow.domain.event;

import java.time.Instant;
import java.util.UUID;

public class DocumentApprovedEvent implements FlowEvent {

    private String eventId;
    private String flowId;
    private Instant timestamp;
    private int stepNumber;
    private String participantEmail;
    private String userId;

    public DocumentApprovedEvent() {
    }

    public DocumentApprovedEvent(String flowId, int stepNumber, String participantEmail, String userId) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.timestamp = Instant.now();
        this.stepNumber = stepNumber;
        this.participantEmail = participantEmail;
        this.userId = userId;
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
}
