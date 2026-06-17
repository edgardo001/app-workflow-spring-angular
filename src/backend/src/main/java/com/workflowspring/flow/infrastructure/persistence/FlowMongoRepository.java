package com.workflowspring.flow.infrastructure.persistence;

import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class FlowMongoRepository {

    private final FlowRepository flowRepository;

    public FlowMongoRepository(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
    }

    public Flow save(Flow flow) {
        return flowRepository.save(flow);
    }

    public Optional<Flow> findById(String id) {
        return flowRepository.findById(id);
    }

    public List<Flow> findByStatus(FlowStatus status) {
        return flowRepository.findByStatus(status);
    }

    public List<Flow> findByCreatedBy(String createdBy) {
        return flowRepository.findByCreatedBy(createdBy);
    }

    public List<Flow> findByDeadlineBeforeAndStatus(Instant deadline, FlowStatus status) {
        return flowRepository.findByDeadlineBeforeAndStatus(deadline, status);
    }

    public Page<Flow> findAll(Pageable pageable) {
        return flowRepository.findAll(pageable);
    }
}
