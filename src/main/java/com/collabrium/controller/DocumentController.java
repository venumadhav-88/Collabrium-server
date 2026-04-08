package com.collabrium.controller;

import com.collabrium.dto.ApiResponse;
import com.collabrium.dto.DocumentDto;
import com.collabrium.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/api/documents")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> getAllDocuments() {
        return ResponseEntity.ok(ApiResponse.success(documentService.getAllDocuments()));
    }

    @GetMapping("/api/projects/{projectId}/documents")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentsByProject(projectId)));
    }

    @PostMapping("/api/projects/{projectId}/documents")
    public ResponseEntity<ApiResponse<DocumentDto>> upload(@PathVariable Long projectId,
                                              @RequestBody DocumentDto dto,
                                              Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(documentService.uploadDocument(projectId, dto, auth.getName())));
    }

    @DeleteMapping("/api/projects/{projectId}/documents/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long documentId) {
        documentService.deleteDocument(projectId, documentId);
        return ResponseEntity.noContent().build();
    }
}
