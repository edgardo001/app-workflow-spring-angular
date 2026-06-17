package com.workflownet.flow.domain.event;

import java.time.Instant;
import java.util.UUID;

public class FlowStartedEvent implements FlowEvent {

    private String eventId;
    private String flowId;
    private Instant timestamp;
    private int currentStep;
    private String participantEmail;

    public FlowStartedEvent() {
    }

    public FlowStartedEvent(String flowId, int currentStep, String participantEmail) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.timestamp = Instant.now();
        this.currentStep = currentStep;
        this.participantEmail = participantEmail;
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

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }
}
