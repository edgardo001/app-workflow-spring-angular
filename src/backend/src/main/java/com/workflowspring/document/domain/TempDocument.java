package com.workflowspring.document.domain;

import java.time.Instant;
import java.util.UUID;

public class TempDocument {

    private String id;
    private String flowId;
    private String fileName;
    private long fileSize;
    private String mimeType;
    private byte[] data;
    private Instant uploadedAt;
    private Instant expiresAt;

    public TempDocument() {
        this.id = UUID.randomUUID().toString();
        this.uploadedAt = Instant.now();
    }

    public TempDocument(String flowId, String fileName, long fileSize, String mimeType, byte[] data) {
        this();
        this.flowId = flowId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.data = data;
        this.expiresAt = Instant.now().plusSeconds(48 * 60 * 60);
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
