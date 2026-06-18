package com.workflowspring.flow.application.dto;

import lombok.Data;

@Data
public class ActionRequest {
    private String reason;
    private String action;
    private String token; // JWS token for approval/rejection
}
