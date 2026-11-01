
package com.employee.management.controller;

import com.employee.management.dto.DocumentResponse;
import com.employee.management.service.DocumentService;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // Point 1: Upload a document
    // The service checks whether the user can access this employee.
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response =
                documentService.uploadDocument(employeeId, file);

        return ResponseEntity.ok(response);
    }

    // Only Admin can view all documents.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {

        return ResponseEntity.ok(
                documentService.getAllDocuments()
        );
    }

    // Point 2: View documents belonging to a specific employee
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByEmployee(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                documentService.getDocumentsByEmployee(employeeId)
        );
    }

    // Point 3: Download a document
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id) {

        Resource resource =
                documentService.downloadDocument(id);

        String filename = resource.getFilename() == null
                ? "document"
                : resource.getFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(resource);
    }

    // Only Admin can delete documents.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id) {

        documentService.deleteDocument(id);

        return ResponseEntity.noContent().build();
    }
}
