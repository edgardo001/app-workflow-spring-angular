package com.workflowspring.flow.application.service;

import com.workflowspring.flow.application.dto.CreateFlowRequest;
import com.workflowspring.flow.application.dto.FlowResponse;
import com.workflowspring.flow.application.mapper.FlowMapper;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import com.workflowspring.flow.infrastructure.persistence.FlowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowServiceTest {

    @Mock
    private FlowRepository flowRepository;

    private FlowMapper flowMapper;
    private FlowService flowService;

    @BeforeEach
    void setUp() {
        flowMapper = new FlowMapper();
        flowService = new FlowService(flowRepository, flowMapper);
    }

    @Test
    void shouldCreateFlow() {
        CreateFlowRequest request = new CreateFlowRequest();
        request.setTitle("Test Flow");
        request.setDescription("Test Description");
        request.setDeadline(Instant.now().plusSeconds(86400));

        Flow savedFlow = new Flow("Test Flow", "Test Description", request.getDeadline(), "user1");
        when(flowRepository.save(any(Flow.class))).thenReturn(savedFlow);

        FlowResponse response = flowService.createFlow(request, "user1");

        assertNotNull(response);
        assertEquals("Test Flow", response.getTitle());
        verify(flowRepository).save(any(Flow.class));
    }

    @Test
    void shouldGetFlowById() {
        Flow flow = new Flow("Test", "Desc", Instant.now().plusSeconds(86400), "user1");
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));

        FlowResponse response = flowService.getFlow("flow1");

        assertEquals("Test", response.getTitle());
    }

    @Test
    void shouldGetAllFlows() {
        Flow f1 = new Flow("Flow1", "Desc1", Instant.now().plusSeconds(86400), "user1");
        Flow f2 = new Flow("Flow2", "Desc2", Instant.now().plusSeconds(86400), "user1");
        when(flowRepository.findAll()).thenReturn(List.of(f1, f2));

        List<FlowResponse> flows = flowService.getAllFlows();

        assertEquals(2, flows.size());
    }

    @Test
    void shouldCancelFlow() {
        Flow flow = new Flow("Test", "Desc", Instant.now().plusSeconds(86400), "user1");
        flow.setStatus(FlowStatus.DRAFT);
        when(flowRepository.findById("flow1")).thenReturn(Optional.of(flow));
        when(flowRepository.save(any(Flow.class))).thenReturn(flow);

        flowService.cancelFlow("flow1", "Admin requested cancellation");

        assertEquals(FlowStatus.CANCELLED, flow.getStatus());
        verify(flowRepository).save(flow);
    }
}
