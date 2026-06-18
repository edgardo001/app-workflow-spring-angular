package com.workflowspring.flow.application.service;

import com.workflowspring.flow.application.dto.CreateFlowRequest;
import com.workflowspring.flow.application.dto.FlowResponse;
import com.workflowspring.flow.application.mapper.FlowMapper;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import com.workflowspring.flow.domain.model.Participant;
import com.workflowspring.flow.domain.service.FlowOrchestratorService;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FlowService {

    private final FlowRepository flowRepository;
    private final FlowMapper flowMapper;
    private final FlowOrchestratorService flowOrchestratorService;

    public FlowService(FlowRepository flowRepository, FlowMapper flowMapper,
                       FlowOrchestratorService flowOrchestratorService) {
        this.flowRepository = flowRepository;
        this.flowMapper = flowMapper;
        this.flowOrchestratorService = flowOrchestratorService;
    }

    public FlowResponse createFlow(CreateFlowRequest request, String createdBy) {
        Flow flow = new Flow(request.getTitle(), request.getDescription(), request.getDeadline(), createdBy);

        if (request.getDestinatarios() != null) {
            for (String email : request.getDestinatarios()) {
                if (email != null && !email.isBlank()) {
                    flow.addParticipant(new Participant(email.trim(), "APPROVER"));
                }
            }
        }

        flowRepository.save(flow);
        flowOrchestratorService.startFlow(flow);
        return flowMapper.toResponse(flow);
    }

    public FlowResponse getFlow(String id) {
        return flowMapper.toResponse(flowRepository.findById(id).orElseThrow());
    }

    public List<FlowResponse> getAllFlows() {
        return flowRepository.findAll().stream().map(flowMapper::toResponse).collect(Collectors.toList());
    }

    public List<FlowResponse> getFlowsByUser(String userId, String userEmail) {
        List<Flow> created = flowRepository.findByCreatedBy(userId);
        List<Flow> participant = flowRepository.findByParticipantsEmail(userEmail);
        return Stream.concat(created.stream(), participant.stream())
                .distinct()
                .map(flowMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<FlowResponse> getPendingFlows(String userEmail) {
        List<Flow> created = flowRepository.findByCreatedBy(userEmail);
        List<Flow> participant = flowRepository.findByParticipantsEmail(userEmail);
        return Stream.concat(created.stream(), participant.stream())
                .distinct()
                .filter(f -> f.getStatus() == FlowStatus.ACTIVE || f.getStatus() == FlowStatus.PENDING_APPROVAL)
                .map(flowMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void cancelFlow(String id, String reason) {
        Flow flow = flowRepository.findById(id).orElseThrow();
        flow.cancel(reason);
        flowRepository.save(flow);
    }
}
