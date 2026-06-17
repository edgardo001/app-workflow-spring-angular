package com.workflowspring.flow.application.dto;

import lombok.Data;

@Data
public class DocumentDto {
    private String id;
    private String filename;
    private String contentType;
    private long size;
}
