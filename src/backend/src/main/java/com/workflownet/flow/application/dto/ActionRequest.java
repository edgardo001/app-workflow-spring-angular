package com.workflownet.flow.application.dto;

import lombok.Data;

@Data
public class ActionRequest {
    private String flowId;
    private String reason;
}
