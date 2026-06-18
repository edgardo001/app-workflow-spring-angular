package com.workflowspring.flow.application.service;

import com.workflowspring.flow.application.dto.CreateFlowRequest;
import com.workflowspring.flow.application.dto.FlowResponse;
import com.workflowspring.flow.application.mapper.FlowMapper;
import com.workflowspring.flow.domain.model.Flow;
import com.workflowspring.flow.domain.model.FlowStatus;
import com.workflowspring.flow.domain.model.Participant;
import com.workflowspring.flow.domain.model.DocumentMetadata;
import com.workflowspring.document.domain.TempDocument;
import com.workflowspring.document.infrastructure.TempDocumentRepository;
import com.workflowspring.document.DocumentService;
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
    private final TempDocumentRepository tempDocumentRepository;
    private final DocumentService documentService;
    private final FlowOrchestratorService flowOrchestratorService;

    public FlowService(FlowRepository flowRepository,
                       FlowMapper flowMapper,
                       TempDocumentRepository tempDocumentRepository,
                       DocumentService documentService,
                       FlowOrchestratorService flowOrchestratorService) {
        this.flowRepository = flowRepository;
        this.flowMapper = flowMapper;
        this.tempDocumentRepository = tempDocumentRepository;
        this.documentService = documentService;
        this.flowOrchestratorService = flowOrchestratorService;
    }

    public FlowResponse createFlow(CreateFlowRequest request, String createdBy) {
        Flow flow = new Flow(request.getTitle(), request.getDescription(), request.getDeadline(), createdBy);

        // Add participants in order
        if (request.getParticipantEmails() != null) {
            for (String email : request.getParticipantEmails()) {
                Participant participant = new Participant(email, "APPROVER");
                flow.addParticipant(participant);
            }
        }

        // Retrieve and add temporary documents
        if (request.getDocumentIds() != null) {
            for (String docId : request.getDocumentIds()) {
                TempDocument tempDoc = tempDocumentRepository.findById(docId)
                        .orElseThrow(() -> new IllegalArgumentException("Document not found: " + docId));
                tempDoc.setFlowId(flow.getId());
                tempDocumentRepository.save(tempDoc);

                DocumentMetadata docMeta = new DocumentMetadata();
                docMeta.setId(tempDoc.getId());
                docMeta.setFileName(tempDoc.getFileName());
                docMeta.setFileSize(tempDoc.getFileSize());
                docMeta.setMimeType(tempDoc.getMimeType());
                docMeta.setTempPath(documentService.getStoragePath(tempDoc.getId(), tempDoc.getFileName()));
                docMeta.setUploadedAt(tempDoc.getUploadedAt());
                
                flow.addDocument(docMeta);
            }
        }

        // Save flow in DRAFT first
        Flow savedFlow = flowRepository.save(flow);

        // Start the flow sequence (marks as ACTIVE, sends first email)
        flowOrchestratorService.startFlow(savedFlow);

        // Retrieve from repository to get updated state (saved by startFlow)
        Flow finalFlow = flowRepository.findById(savedFlow.getId()).orElse(savedFlow);

        return flowMapper.toResponse(finalFlow);
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
        return flowRepository.findByParticipantsEmail(userEmail).stream()
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
