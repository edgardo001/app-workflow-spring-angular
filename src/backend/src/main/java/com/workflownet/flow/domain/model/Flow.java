package com.workflownet.flow.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Flow {

    private static final int MAX_DOCUMENTS = 5;

    private String id;
    private String title;
    private String description;
    private FlowStatus status;
    private List<DocumentMetadata> documents;
    private List<Participant> participants;
    private int currentStep;
    private Instant deadline;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private int gracePeriodDays;
    private String rejectedReason;

    public Flow() {
        this.id = UUID.randomUUID().toString();
        this.status = FlowStatus.DRAFT;
        this.documents = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.currentStep = -1;
        this.gracePeriodDays = 3;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Flow(String title, String description, Instant deadline, String createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.createdBy = createdBy;
    }

    public void start() {
        if (status != FlowStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT flows can be started");
        }
        if (participants.isEmpty()) {
            throw new IllegalStateException("Cannot start a flow without participants");
        }
        if (documents.isEmpty()) {
            throw new IllegalStateException("Cannot start a flow without documents");
        }
        this.status = FlowStatus.ACTIVE;
        this.currentStep = 0;
        this.updatedAt = Instant.now();
    }

    public void approve(String participantEmail) {
        if (status != FlowStatus.ACTIVE && status != FlowStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Flow is not active");
        }
        Participant current = participants.get(currentStep);
        if (!current.getEmail().equals(participantEmail)) {
            throw new IllegalArgumentException("Participant at current step does not match");
        }
        current.setApproved(true);
        current.setApprovedAt(Instant.now());
        if (currentStep == participants.size() - 1) {
            this.status = FlowStatus.COMPLETED;
        } else {
            this.currentStep++;
            this.status = FlowStatus.PENDING_APPROVAL;
        }
        this.updatedAt = Instant.now();
    }

    public void reject(String participantEmail, String reason) {
        if (status != FlowStatus.ACTIVE && status != FlowStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Flow is not active");
        }
        Participant current = participants.get(currentStep);
        if (!current.getEmail().equals(participantEmail)) {
            throw new IllegalArgumentException("Participant at current step does not match");
        }
        current.setRejected(true);
        current.setRejectedReason(reason);
        this.status = FlowStatus.REJECTED;
        this.rejectedReason = reason;
        this.updatedAt = Instant.now();
    }

    public void expire() {
        if (status != FlowStatus.ACTIVE && status != FlowStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only active flows can expire");
        }
        this.status = FlowStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (status == FlowStatus.COMPLETED || status == FlowStatus.CANCELLED) {
            throw new IllegalStateException("Flow cannot be cancelled in its current state");
        }
        this.status = FlowStatus.CANCELLED;
        this.rejectedReason = reason;
        this.updatedAt = Instant.now();
    }

    public void addDocument(DocumentMetadata document) {
        if (documents.size() >= MAX_DOCUMENTS) {
            throw new IllegalStateException("Maximum of " + MAX_DOCUMENTS + " documents allowed");
        }
        this.documents.add(document);
        this.updatedAt = Instant.now();
    }

    public void addParticipant(Participant participant) {
        participant.setStepOrder(this.participants.size());
        this.participants.add(participant);
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public void setStatus(FlowStatus status) {
        this.status = status;
    }

    public List<DocumentMetadata> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentMetadata> documents) {
        this.documents = documents;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }
}
