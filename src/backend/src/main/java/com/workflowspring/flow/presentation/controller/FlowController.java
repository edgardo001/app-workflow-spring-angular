package com.workflowspring.flow.presentation.controller;

import com.workflowspring.flow.application.dto.ActionRequest;
import com.workflowspring.flow.application.dto.CreateFlowRequest;
import com.workflowspring.flow.application.dto.FlowResponse;
import com.workflowspring.flow.application.service.FlowService;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.service.FlowOrchestratorService;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flows")
public class FlowController {

    private final FlowService flowService;
    private final FlowOrchestratorService flowOrchestratorService;
    private final FlowRepository flowRepository;

    public FlowController(FlowService flowService,
                          FlowOrchestratorService flowOrchestratorService,
                          FlowRepository flowRepository) {
        this.flowService = flowService;
        this.flowOrchestratorService = flowOrchestratorService;
        this.flowRepository = flowRepository;
    }

    @PostMapping
    public FlowResponse create(@RequestBody CreateFlowRequest request) {
        return flowService.createFlow(request, getCurrentUserId());
    }

    @GetMapping
    public List<FlowResponse> getAll() {
        String userId = getCurrentUserId();
        return flowService.getFlowsByUser(userId, userId);
    }

    @GetMapping("/{id}")
    public FlowResponse getById(@PathVariable String id) {
        return flowService.getFlow(id);
    }

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable String id, @RequestBody ActionRequest request) {
        String userId = getCurrentUserId();
        Flow flow = flowRepository.findById(id).orElseThrow();
        flowOrchestratorService.processApproval(id, flow.getCurrentStep(), userId, userId);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable String id, @RequestBody ActionRequest request) {
        String userId = getCurrentUserId();
        Flow flow = flowRepository.findById(id).orElseThrow();
        flowOrchestratorService.processRejection(id, flow.getCurrentStep(), userId, userId, request.getReason());
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable String id, @RequestBody ActionRequest request) {
        flowService.cancelFlow(id, request.getReason());
    }

    @PostMapping("/{id}/start")
    public FlowResponse start(@PathVariable String id) {
        Flow flow = flowRepository.findById(id).orElseThrow();
        flowOrchestratorService.startFlow(flow);
        return flowService.getFlow(id);
    }

    @GetMapping("/pending")
    public List<FlowResponse> getPending() {
        return flowService.getPendingFlows(getCurrentUserId());
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymousUser";
    }
}
