package com.workflowspring.flow.application.dto;

import lombok.Data;

@Data
public class DocumentDto {
    private String id;
    private String filename;
    private String name;
    private String contentType;
    private String type;
    private String size;
}
