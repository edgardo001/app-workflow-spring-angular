package com.workflownet.document.infrastructure;

import com.workflownet.document.domain.TempDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempDocumentRepository extends MongoRepository<TempDocument, String> {

    List<TempDocument> findByFlowId(String flowId);

    void deleteByFlowId(String flowId);
}
