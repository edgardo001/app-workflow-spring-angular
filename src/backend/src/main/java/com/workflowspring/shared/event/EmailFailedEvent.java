package com.workflowspring.shared.event;

import java.time.Instant;
import java.util.UUID;

public class EmailFailedEvent {

    private String eventId;
    private String flowId;
    private String to;
    private String subject;
    private String errorMessage;
    private int attemptNumber;
    private Instant timestamp;

    public EmailFailedEvent() {
    }

    public EmailFailedEvent(String flowId, String to, String subject, String errorMessage, int attemptNumber) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.to = to;
        this.subject = subject;
        this.errorMessage = errorMessage;
        this.attemptNumber = attemptNumber;
        this.timestamp = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
