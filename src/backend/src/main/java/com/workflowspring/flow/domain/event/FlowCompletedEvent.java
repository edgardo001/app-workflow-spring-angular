package com.workflowspring.flow.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FlowCompletedEvent implements FlowEvent {

    private String eventId;
    private String flowId;
    private Instant timestamp;
    private List<String> finalDocumentHashes;

    public FlowCompletedEvent() {
    }

    public FlowCompletedEvent(String flowId, List<String> finalDocumentHashes) {
        this.eventId = UUID.randomUUID().toString();
        this.flowId = flowId;
        this.timestamp = Instant.now();
        this.finalDocumentHashes = finalDocumentHashes;
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

    public List<String> getFinalDocumentHashes() {
        return finalDocumentHashes;
    }

    public void setFinalDocumentHashes(List<String> finalDocumentHashes) {
        this.finalDocumentHashes = finalDocumentHashes;
    }
}
