package com.workflowspring.flow.application.mapper;

import com.workflowspring.flow.application.dto.DocumentDto;
import com.workflowspring.flow.application.dto.FlowResponse;
import com.workflowspring.flow.application.dto.ParticipantDto;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import com.workflowspring.flow.domain.model.Participant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FlowMapper {

    public FlowResponse toResponse(Flow flow) {
        FlowResponse response = new FlowResponse();
        response.setId(flow.getId());
        response.setTitle(flow.getTitle());
        response.setDescription(flow.getDescription());
        response.setStatus(flow.getStatus());
        response.setDeadline(flow.getDeadline());
        response.setCreatedBy(flow.getCreatedBy());
        response.setOwnerEmail(flow.getCreatedBy());
        response.setStep(flow.getCurrentStep());
        response.setTotalSteps(flow.getParticipants().size());
        response.setCreatedAt(flow.getCreatedAt());

        response.setParticipants(flow.getParticipants().stream()
                .map(p -> {
                    ParticipantDto dto = new ParticipantDto();
                    dto.setEmail(p.getEmail());
                    String name = p.getEmail().contains("@") ? p.getEmail().split("@")[0] : p.getEmail();
                    dto.setName(name.substring(0, 1).toUpperCase() + (name.length() > 1 ? name.substring(1) : ""));
                    dto.setRole(p.getRole());
                    dto.setStepOrder(p.getStepOrder());
                    dto.setStatus(p.isApproved() ? "approved" : (p.isRejected() ? "rejected" : "pending"));
                    dto.setApprovedAt(p.getApprovedAt());
                    return dto;
                })
                .collect(Collectors.toList()));

        if (flow.getDocuments() != null) {
            response.setDocuments(flow.getDocuments().stream()
                    .map(doc -> {
                        DocumentDto dto = new DocumentDto();
                        dto.setId(doc.getId());
                        dto.setFilename(doc.getFileName());
                        dto.setName(doc.getFileName());
                        dto.setContentType(doc.getMimeType());
                        String ext = "";
                        if (doc.getFileName() != null && doc.getFileName().contains(".")) {
                            ext = doc.getFileName().substring(doc.getFileName().lastIndexOf(".") + 1).toLowerCase();
                        }
                        dto.setType(ext);
                        
                        long bytes = doc.getFileSize();
                        if (bytes >= 1024 * 1024) {
                            dto.setSize(String.format("%.1f MB", (double) bytes / (1024 * 1024)));
                        } else if (bytes >= 1024) {
                            dto.setSize(String.format("%.1f KB", (double) bytes / 1024));
                        } else {
                            dto.setSize(bytes + " B");
                        }
                        return dto;
                    })
                    .collect(Collectors.toList()));
        }

        String currentUserEmail = getCurrentUserEmail();
        boolean isMyTurn = false;
        if (currentUserEmail != null && (flow.getStatus() == FlowStatus.ACTIVE || flow.getStatus() == FlowStatus.PENDING_APPROVAL)) {
            int currentStep = flow.getCurrentStep();
            if (currentStep >= 0 && currentStep < flow.getParticipants().size()) {
                Participant currentParticipant = flow.getParticipants().get(currentStep);
                isMyTurn = currentUserEmail.equalsIgnoreCase(currentParticipant.getEmail());
            }
        }
        response.setMyTurn(isMyTurn);

        return response;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) ? auth.getName() : null;
    }
}
