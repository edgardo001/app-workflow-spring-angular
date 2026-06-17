package com.workflowspring.document;

import com.workflowspring.flow.domain.model.DocumentMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentMetadata> upload(@RequestParam("file") MultipartFile file) {
        DocumentMetadata metadata = documentService.upload(file);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/flow/{flowId}")
    public ResponseEntity<List<DocumentMetadata>> getDocumentsByFlow(@PathVariable String flowId) {
        List<DocumentMetadata> documents = documentService.getDocumentsByFlow(flowId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        var tempDoc = documentService.getDocument(id);
        String path = documentService.getStoragePath(id, tempDoc.getFileName());
        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + tempDoc.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
