package com.workflownet.document;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file) {
        documentService.upload(file);
    }
}
