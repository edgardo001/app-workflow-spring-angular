package com.workflowspring.flow.domain.event;

import java.time.Instant;
import java.util.UUID;

public class FlowCreatedEvent implements FlowEvent {

    private String eventId;
    private String flowId;
    private Instant timestamp;
    private String title;
    private String createdBy;

    public FlowCreatedEvent() {
    }

    public FlowCreatedEvent(String flowId, String title, String createdBy) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.timestamp = Instant.now();
        this.title = title;
        this.createdBy = createdBy;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
