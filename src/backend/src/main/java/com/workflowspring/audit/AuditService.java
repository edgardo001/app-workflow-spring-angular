package com.workflowspring.audit;

import com.workflowspring.audit.domain.FlowAuditLog;
import com.workflowspring.audit.infrastructure.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logEvent(String flowId, String action, String userId, String userEmail,
                         String documentHash, Map<String, Object> metadata) {
        Map<String, String> stringMetadata = null;
        if (metadata != null) {
            stringMetadata = new HashMap<>();
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                stringMetadata.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }
        FlowAuditLog log = new FlowAuditLog(flowId, action, userId, userEmail, documentHash, stringMetadata);
        auditLogRepository.save(log);
    }

    public void logEvent(String flowId, String action) {
        logEvent(flowId, action, null, null, null, null);
    }

    public List<FlowAuditLog> getAuditLogByFlow(String flowId) {
        return auditLogRepository.findByFlowIdOrderByTimestampDesc(flowId);
    }
}
