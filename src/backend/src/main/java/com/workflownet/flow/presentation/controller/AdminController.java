package com.workflownet.flow.presentation.controller;

import com.workflownet.flow.application.dto.ActionRequest;
import com.workflownet.flow.application.dto.FlowResponse;
import com.workflownet.flow.application.mapper.FlowMapper;
import com.workflownet.flow.domain.model.Flow;
import com.workflownet.flow.domain.model.FlowStatus;
import com.workflownet.flow.domain.service.FlowOrchestratorService;
import com.workflownet.flow.infrastructure.persistence.FlowRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final FlowRepository flowRepository;
    private final FlowMapper flowMapper;
    private final FlowOrchestratorService flowOrchestratorService;

    public AdminController(FlowRepository flowRepository,
                           FlowMapper flowMapper,
                           FlowOrchestratorService flowOrchestratorService) {
        this.flowRepository = flowRepository;
        this.flowMapper = flowMapper;
        this.flowOrchestratorService = flowOrchestratorService;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Flow> all = flowRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", all.size());
        stats.put("draft", all.stream().filter(f -> f.getStatus() == FlowStatus.DRAFT).count());
        stats.put("active", all.stream().filter(f -> f.getStatus() == FlowStatus.ACTIVE).count());
        stats.put("pendingApproval", all.stream().filter(f -> f.getStatus() == FlowStatus.PENDING_APPROVAL).count());
        stats.put("completed", all.stream().filter(f -> f.getStatus() == FlowStatus.COMPLETED).count());
        stats.put("rejected", all.stream().filter(f -> f.getStatus() == FlowStatus.REJECTED).count());
        stats.put("expired", all.stream().filter(f -> f.getStatus() == FlowStatus.EXPIRED).count());
        stats.put("cancelled", all.stream().filter(f -> f.getStatus() == FlowStatus.CANCELLED).count());
        stats.put("pending", all.stream().filter(f -> f.getStatus() == FlowStatus.ACTIVE || f.getStatus() == FlowStatus.PENDING_APPROVAL).count());
        return stats;
    }

    @GetMapping("/flows")
    public List<FlowResponse> getAllFlows() {
        return flowRepository.findAll().stream()
                .map(flowMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/flows/{id}/reject")
    public void rejectFlow(@PathVariable String id, @RequestBody ActionRequest request) {
        Flow flow = flowRepository.findById(id).orElseThrow();
        flow.setStatus(FlowStatus.REJECTED);
        flow.setRejectedReason(request.getReason());
        flowRepository.save(flow);
    }

    @PostMapping("/flows/{id}/relaunch")
    public void relaunchFlow(@PathVariable String id) {
        flowOrchestratorService.repairFlow(id);
    }
}
