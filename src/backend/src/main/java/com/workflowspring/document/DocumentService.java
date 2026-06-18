package com.workflowspring.document;

import com.workflowspring.document.domain.TempDocument;
import com.workflowspring.document.infrastructure.TempDocumentRepository;
import com.workflowspring.flow.domain.model.DocumentMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final TempDocumentRepository tempDocumentRepository;
    private final String tempStorage;

    public DocumentService(TempDocumentRepository tempDocumentRepository,
                           @Value("${workflow.document.temp-storage}") String tempStorage) {
        this.tempDocumentRepository = tempDocumentRepository;
        this.tempStorage = tempStorage;
    }

    public DocumentMetadata upload(MultipartFile file) {
        if (file.getSize() > 2097152) {
            throw new IllegalArgumentException("File size exceeds maximum of 2MB");
        }

        String id = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String storagePath = Paths.get(tempStorage, id + "-" + originalFilename).toString();

        File dest = new File(storagePath).getAbsoluteFile();
        dest.getParentFile().mkdirs();
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }

        String hash = computeHash(dest);

        TempDocument tempDoc = new TempDocument();
        tempDoc.setId(id);
        tempDoc.setFileName(originalFilename);
        tempDoc.setFileSize(file.getSize());
        tempDoc.setMimeType(file.getContentType());
        tempDoc.setData(null);
        tempDoc.setUploadedAt(Instant.now());
        tempDoc.setExpiresAt(Instant.now().plusSeconds(48 * 60 * 60));
        tempDocumentRepository.save(tempDoc);

        DocumentMetadata meta = new DocumentMetadata();
        meta.setId(id);
        meta.setFileName(originalFilename);
        meta.setFileSize(file.getSize());
        meta.setMimeType(file.getContentType());
        meta.setTempPath(storagePath);
        meta.setHash(hash);
        meta.setUploadedAt(Instant.now());

        return meta;
    }

    public List<DocumentMetadata> getDocumentsByFlow(String flowId) {
        return tempDocumentRepository.findByFlowId(flowId)
                .stream()
                .map(this::toMetadata)
                .collect(Collectors.toList());
    }

    public TempDocument getDocument(String id) {
        return tempDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
    }

    public String computeHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest.digest()) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }

    public void cleanupByFlow(String flowId) {
        List<TempDocument> docs = tempDocumentRepository.findByFlowId(flowId);
        for (TempDocument doc : docs) {
            deleteFile(doc);
        }
        tempDocumentRepository.deleteByFlowId(flowId);
    }

    public String getStoragePath(String id, String fileName) {
        return Paths.get(tempStorage, id + "-" + fileName).toString();
    }

    public void deleteDocument(String id) {
        TempDocument doc = getDocument(id);
        deleteFile(doc);
        tempDocumentRepository.delete(doc);
    }

    private void deleteFile(TempDocument doc) {
        String path = Paths.get(tempStorage, doc.getId() + "-" + doc.getFileName()).toString();
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    private DocumentMetadata toMetadata(TempDocument doc) {
        DocumentMetadata meta = new DocumentMetadata();
        meta.setId(doc.getId());
        meta.setFileName(doc.getFileName());
        meta.setFileSize(doc.getFileSize());
        meta.setMimeType(doc.getMimeType());
        meta.setUploadedAt(doc.getUploadedAt());
        return meta;
    }
}
