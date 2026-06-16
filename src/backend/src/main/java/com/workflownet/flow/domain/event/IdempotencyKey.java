package com.workflownet.flow.domain.event;

import java.time.Instant;

public class IdempotencyKey {

    private String id;
    private String key;
    private Instant processedAt;

    public IdempotencyKey() {
    }

    public IdempotencyKey(String id, String key, Instant processedAt) {
        this.id = id;
        this.key = key;
        this.processedAt = processedAt;
    }

    public IdempotencyKey(String key) {
        this.key = key;
        this.processedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
