package com.workflowspring.flow.application.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class CreateFlowRequest {
    private String title;
    private String description;
    private Instant deadline;
    private List<String> participantEmails;
}
