package com.workflownet.flow.domain.model;

import java.time.Instant;
import java.util.UUID;

public class DocumentMetadata {

    private String id;
    private String fileName;
    private long fileSize;
    private String mimeType;
    private String tempPath;
    private String hash;
    private Instant uploadedAt;

    public DocumentMetadata() {
        this.id = UUID.randomUUID().toString();
        this.uploadedAt = Instant.now();
    }

    public DocumentMetadata(String fileName, long fileSize, String mimeType, String tempPath) {
        this();
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.tempPath = tempPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
