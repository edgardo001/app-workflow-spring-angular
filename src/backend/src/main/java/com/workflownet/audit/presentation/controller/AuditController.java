package com.workflownet.audit.presentation.controller;

import com.workflownet.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;
    
    @GetMapping("/flow/{flowId}")
    public void getAuditLog(@PathVariable String flowId) {}
}
