package com.workflownet.flow.application.mapper;

import com.workflownet.flow.application.dto.FlowResponse;
import com.workflownet.flow.application.dto.ParticipantDto;
import com.workflownet.flow.domain.model.Flow;
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
        response.setParticipants(flow.getParticipants().stream()
                .map(p -> {
                    ParticipantDto dto = new ParticipantDto();
                    dto.setEmail(p.getEmail());
                    dto.setRole(p.getRole());
                    return dto;
                })
                .collect(Collectors.toList()));
        return response;
    }
}
