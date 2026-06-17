package com.workflowspring.audit.presentation.controller;

import com.workflowspring.audit.domain.FlowAuditLog;
import com.workflowspring.audit.infrastructure.AuditLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/flow/{flowId}")
    public List<FlowAuditLog> getAuditLog(@PathVariable String flowId) {
        return auditLogRepository.findByFlowIdOrderByTimestampDesc(flowId);
    }
}
