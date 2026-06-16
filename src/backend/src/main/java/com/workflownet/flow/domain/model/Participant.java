package com.workflownet.flow.domain.model;

import java.time.Instant;

public class Participant {

    private String email;
    private String name;
    private int stepOrder;
    private String token;
    private boolean approved;
    private Instant approvedAt;
    private boolean rejected;
    private String rejectedReason;

    public Participant() {
    }

    public Participant(String email, String name, int stepOrder) {
        this.email = email;
        this.name = name;
        this.stepOrder = stepOrder;
        this.approved = false;
        this.rejected = false;
    }

    public Participant(String email, String name, int stepOrder, String token) {
        this(email, name, stepOrder);
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }
}
