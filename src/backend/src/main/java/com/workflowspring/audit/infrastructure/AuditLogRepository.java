package com.workflowspring.audit.infrastructure;

import com.workflowspring.audit.domain.FlowAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<FlowAuditLog, String> {

    List<FlowAuditLog> findByFlowIdOrderByTimestampDesc(String flowId);

    List<FlowAuditLog> findByAction(String action);
}
