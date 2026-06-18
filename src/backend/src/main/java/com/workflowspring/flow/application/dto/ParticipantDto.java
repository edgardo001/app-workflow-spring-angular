package com.workflowspring.flow.application.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ParticipantDto {
    private String email;
    private String name;
    private String role;
    private int stepOrder;
    private String status;
    private Instant approvedAt;
}
