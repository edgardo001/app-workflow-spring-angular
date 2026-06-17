package com.workflownet.flow.application.dto;

import com.workflownet.flow.domain.model.FlowStatus;
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
    private List<ParticipantDto> participants;
}
