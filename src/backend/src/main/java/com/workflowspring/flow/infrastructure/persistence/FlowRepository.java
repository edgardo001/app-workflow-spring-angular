package com.workflowspring.flow.infrastructure.persistence;

import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FlowRepository extends MongoRepository<Flow, String> {

    List<Flow> findByStatus(FlowStatus status);

    List<Flow> findByCreatedBy(String createdBy);

    List<Flow> findByParticipantsEmail(String email);

    List<Flow> findByDeadlineBeforeAndStatus(Instant deadline, FlowStatus status);
}
