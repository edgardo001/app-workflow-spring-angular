package com.workflowspring.flow.domain.event;

import java.time.Instant;
import java.util.UUID;

public class FlowExpiredEvent implements FlowEvent {

    private String eventId;
    private String flowId;
    private Instant timestamp;

    public FlowExpiredEvent() {
    }

    public FlowExpiredEvent(String flowId) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.timestamp = Instant.now();
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
}
