package com.workflownet.flow.application.service;

import com.workflownet.flow.application.dto.CreateFlowRequest;
import com.workflownet.flow.application.dto.FlowResponse;
import com.workflownet.flow.application.mapper.FlowMapper;
import com.workflownet.flow.domain.model.Flow;
import com.workflownet.flow.domain.persistence.FlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowService {
    private final FlowRepository flowRepository;
    private final FlowMapper flowMapper;

    public FlowResponse createFlow(CreateFlowRequest request) {
        Flow flow = Flow.create(request.getTitle(), request.getDescription(), request.getDeadline());
        return flowMapper.toResponse(flowRepository.save(flow));
    }

    public FlowResponse getFlow(String id) {
        return flowMapper.toResponse(flowRepository.findById(id).orElseThrow());
    }

    public List<FlowResponse> getAllFlows() {
        return flowRepository.findAll().stream().map(flowMapper::toResponse).collect(Collectors.toList());
    }

    public void cancelFlow(String id) {
        Flow flow = flowRepository.findById(id).orElseThrow();
        flow.cancel();
        flowRepository.save(flow);
    }
}
