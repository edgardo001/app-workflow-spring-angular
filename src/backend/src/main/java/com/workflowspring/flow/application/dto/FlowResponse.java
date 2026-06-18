package com.workflowspring.flow.application.dto;

import com.workflowspring.flow.domain.model.FlowStatus;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class FlowResponse {
    private String id;
    private String title;
    private String description;
    private FlowStatus status;
    private Instant deadline;
    private String createdBy;
    private String ownerEmail;
    private int step;
    private int totalSteps;
    private List<ParticipantDto> participants;
    private List<DocumentDto> documents;
    private boolean isMyTurn;
    private Instant createdAt;
}
