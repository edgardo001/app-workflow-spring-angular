package com.workflowspring.flow.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FlowTest {

    private Flow flow;

    @BeforeEach
    void setUp() {
        flow = new Flow();
        flow.setTitle("Test Flow");
        flow.setStatus(FlowStatus.DRAFT);
    }

    private DocumentMetadata createDoc(String name) {
        return new DocumentMetadata(name, 1024L, "application/pdf", "/tmp/" + name);
    }

    @Test
    void shouldCreateFlowInDraftStatus() {
        Flow f = new Flow("Test", "Desc", Instant.now().plusSeconds(86400), "user1");
        assertEquals(FlowStatus.DRAFT, f.getStatus());
        assertNotNull(f.getId());
        assertEquals(-1, f.getCurrentStep());
    }

    @Test
    void shouldStartFlow() {
        flow.addDocument(createDoc("doc1"));
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        flow.start();
        assertEquals(FlowStatus.ACTIVE, flow.getStatus());
        assertEquals(0, flow.getCurrentStep());
    }

    @Test
    void shouldThrowWhenStartingWithoutParticipants() {
        flow.addDocument(createDoc("doc1"));
        assertThrows(IllegalStateException.class, flow::start);
    }

    @Test
    void shouldThrowWhenStartingWithoutDocuments() {
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        assertThrows(IllegalStateException.class, flow::start);
    }

    @Test
    void shouldApproveStepAndAdvance() {
        flow.addDocument(createDoc("doc1"));
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        flow.addParticipant(new Participant("bob@test.com", "approver"));
        flow.start();
        flow.approve("alice@test.com");
        assertEquals(1, flow.getCurrentStep());
        assertEquals(FlowStatus.PENDING_APPROVAL, flow.getStatus());
    }

    @Test
    void shouldCompleteFlowOnLastApproval() {
        flow.addDocument(createDoc("doc1"));
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        flow.start();
        flow.approve("alice@test.com");
        assertEquals(FlowStatus.COMPLETED, flow.getStatus());
    }

    @Test
    void shouldRejectFlow() {
        flow.addDocument(createDoc("doc1"));
        flow.addParticipant(new Participant("alice@test.com", "approver"));
        flow.start();
        flow.reject("alice@test.com", "Not approved");
        assertEquals(FlowStatus.REJECTED, flow.getStatus());
        assertEquals("Not approved", flow.getRejectedReason());
    }

    @Test
    void shouldExpireActiveFlow() {
        flow.setStatus(FlowStatus.ACTIVE);
        flow.expire();
        assertEquals(FlowStatus.EXPIRED, flow.getStatus());
    }

    @Test
    void shouldCancelFlow() {
        flow.setStatus(FlowStatus.DRAFT);
        flow.cancel("Cancelled by admin");
        assertEquals(FlowStatus.CANCELLED, flow.getStatus());
    }

    @Test
    void shouldEnforceMaxDocuments() {
        for (int i = 0; i < 5; i++) {
            flow.addDocument(createDoc("doc" + i));
        }
        assertThrows(IllegalStateException.class,
                () -> flow.addDocument(createDoc("doc6")));
    }
}
