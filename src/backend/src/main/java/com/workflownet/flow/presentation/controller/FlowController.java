package com.workflownet.flow.presentation.controller;

import com.workflownet.flow.application.dto.ActionRequest;
import com.workflownet.flow.application.dto.CreateFlowRequest;
import com.workflownet.flow.application.dto.FlowResponse;
import com.workflownet.flow.application.service.FlowService;
import com.workflownet.flow.domain.model.Flow;
import com.workflownet.flow.domain.service.FlowOrchestratorService;
import com.workflownet.flow.infrastructure.persistence.FlowRepository;
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

    @GetMapping("/pending")
    public List<FlowResponse> getPending() {
        return flowService.getPendingFlows(getCurrentUserId());
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymousUser";
    }
}
