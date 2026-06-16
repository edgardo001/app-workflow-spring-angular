package com.workflownet.flow.application.service;

import com.workflownet.flow.application.dto.CreateFlowRequest;
import com.workflownet.flow.application.dto.FlowResponse;
import com.workflownet.flow.application.mapper.FlowMapper;
import com.workflownet.flow.domain.model.Flow;
import com.workflownet.flow.infrastructure.persistence.FlowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlowService {
    private final FlowRepository flowRepository;
    private final FlowMapper flowMapper;

    public FlowService(FlowRepository flowRepository, FlowMapper flowMapper) {
        this.flowRepository = flowRepository;
        this.flowMapper = flowMapper;
    }

    public FlowResponse createFlow(CreateFlowRequest request, String createdBy) {
        Flow flow = new Flow(request.getTitle(), request.getDescription(), request.getDeadline(), createdBy);
        return flowMapper.toResponse(flowRepository.save(flow));
    }

    public FlowResponse getFlow(String id) {
        return flowMapper.toResponse(flowRepository.findById(id).orElseThrow());
    }

    public List<FlowResponse> getAllFlows() {
        return flowRepository.findAll().stream().map(flowMapper::toResponse).collect(Collectors.toList());
    }

    public void cancelFlow(String id, String reason) {
        Flow flow = flowRepository.findById(id).orElseThrow();
        flow.cancel(reason);
        flowRepository.save(flow);
    }
}
