package com.workflowspring.shared.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EmailSendEvent {

    private String eventId;
    private String flowId;
    private String to;
    private String subject;
    private String body;
    private List<String> attachmentPaths;
    private int stepNumber;
    private Instant timestamp;

    public EmailSendEvent() {
    }

    public EmailSendEvent(String flowId, String to, String subject, String body,
                          List<String> attachmentPaths, int stepNumber) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachmentPaths = attachmentPaths;
        this.stepNumber = stepNumber;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getAttachmentPaths() {
        return attachmentPaths;
    }

    public void setAttachmentPaths(List<String> attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
