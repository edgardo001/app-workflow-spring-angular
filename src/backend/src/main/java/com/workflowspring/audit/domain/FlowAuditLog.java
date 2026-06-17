package com.workflowspring.audit.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class FlowAuditLog {

    private String id;
    private String flowId;
    private String action;
    private String userId;
    private String userEmail;
    private Instant timestamp;
    private String documentHash;
    private Map<String, String> metadata;

    public FlowAuditLog() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public FlowAuditLog(String flowId, String action, String userId, String userEmail) {
        this();
        this.flowId = flowId;
        this.action = action;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public FlowAuditLog(String flowId, String action, String userId, String userEmail,
                        String documentHash, Map<String, String> metadata) {
        this(flowId, action, userId, userEmail);
        this.documentHash = documentHash;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
