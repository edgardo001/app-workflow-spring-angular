package com.workflowspring.flow.presentation.controller;

import com.workflowspring.flow.application.dto.ActionRequest;
import com.workflowspring.flow.application.dto.CreateFlowRequest;
import com.workflowspring.flow.application.dto.FlowResponse;
import com.workflowspring.flow.application.service.FlowService;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.auth.JwtTokenService;
import com.workflowspring.flow.domain.service.FlowOrchestratorService;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import com.workflowspring.flow.application.mapper.FlowMapper;
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
    private final JwtTokenService jwtTokenService;
    private final FlowMapper flowMapper;

    public FlowController(FlowService flowService,
                          FlowOrchestratorService flowOrchestratorService,
                          FlowRepository flowRepository,
                          JwtTokenService jwtTokenService,
                          FlowMapper flowMapper) {
        this.flowService = flowService;
        this.flowOrchestratorService = flowOrchestratorService;
        this.flowRepository = flowRepository;
        this.jwtTokenService = jwtTokenService;
        this.flowMapper = flowMapper;
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
    public FlowResponse approve(@PathVariable String id, @RequestBody ActionRequest request) {
        String participantEmail;
        if (request.getToken() != null && !request.getToken().trim().isEmpty()) {
            if (!jwtTokenService.validateApprovalToken(request.getToken())) {
                throw new IllegalArgumentException("Invalid approval token");
            }
            String tokenFlowId = jwtTokenService.getFlowIdFromToken(request.getToken());
            participantEmail = jwtTokenService.getEmailFromToken(request.getToken());
            if (!id.equals(tokenFlowId)) {
                throw new IllegalArgumentException("Token flow ID does not match request path");
            }
        } else {
            String currentUserEmail = getCurrentUserId();
            if ("anonymousUser".equals(currentUserEmail)) {
                throw new IllegalArgumentException("Authentication required to approve without token");
            }
            participantEmail = currentUserEmail;
        }

        String authenticatedEmail = getCurrentUserId();
        if ("anonymousUser".equals(authenticatedEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        if (!authenticatedEmail.equalsIgnoreCase(participantEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Authenticated user " + authenticatedEmail + " is not authorized for participant " + participantEmail
            );
        }

        Flow flow = flowRepository.findById(id).orElseThrow();
        Flow updated = flowOrchestratorService.processApproval(id, flow.getCurrentStep(), participantEmail, authenticatedEmail);
        return flowMapper.toResponse(updated);
    }

    @PostMapping("/{id}/reject")
    public FlowResponse reject(@PathVariable String id, @RequestBody ActionRequest request) {
        String participantEmail;
        if (request.getToken() != null && !request.getToken().trim().isEmpty()) {
            if (!jwtTokenService.validateApprovalToken(request.getToken())) {
                throw new IllegalArgumentException("Invalid approval token");
            }
            String tokenFlowId = jwtTokenService.getFlowIdFromToken(request.getToken());
            participantEmail = jwtTokenService.getEmailFromToken(request.getToken());
            if (!id.equals(tokenFlowId)) {
                throw new IllegalArgumentException("Token flow ID does not match request path");
            }
        } else {
            String currentUserEmail = getCurrentUserId();
            if ("anonymousUser".equals(currentUserEmail)) {
                throw new IllegalArgumentException("Authentication required to reject without token");
            }
            participantEmail = currentUserEmail;
        }

        String authenticatedEmail = getCurrentUserId();
        if ("anonymousUser".equals(authenticatedEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        if (!authenticatedEmail.equalsIgnoreCase(participantEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Authenticated user " + authenticatedEmail + " is not authorized for participant " + participantEmail
            );
        }

        Flow flow = flowRepository.findById(id).orElseThrow();
        Flow updated = flowOrchestratorService.processRejection(id, flow.getCurrentStep(), participantEmail, authenticatedEmail, request.getReason());
        return flowMapper.toResponse(updated);
    }

    @GetMapping("/verify")
    public FlowResponse verifyToken(@RequestParam("token") String token) {
        if (token == null || !jwtTokenService.validateApprovalToken(token)) {
            throw new IllegalArgumentException("Invalid or missing approval token");
        }
        String flowId = jwtTokenService.getFlowIdFromToken(token);
        return flowService.getFlow(flowId);
    }

    @PostMapping("/{id}/cancel")
    public FlowResponse cancel(@PathVariable String id, @RequestBody ActionRequest request) {
        flowService.cancelFlow(id, request.getReason());
        Flow flow = flowRepository.findById(id).orElseThrow();
        return flowMapper.toResponse(flow);
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
