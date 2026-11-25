package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.document.DocumentRequest;
import com.hcl.hrgenius.dto.document.DocumentResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityHelper securityHelper;

    public DocumentController(DocumentService documentService, SecurityHelper securityHelper) {
        this.documentService = documentService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {

        List<DocumentResponse> documents = documentService.getDocuments(search, status, type);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            Employee emp = empOpt.get();
            List<DocumentResponse> ownDocs = documents.stream()
                    .filter(d -> (emp.getName() != null && emp.getName().equalsIgnoreCase(d.getEmployee())))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownDocs);
        }

        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id, HttpServletRequest request) {
        DocumentResponse document = documentService.getDocumentById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty() || !empOpt.get().getName().equalsIgnoreCase(document.getEmployee())) {
                throw new ForbiddenException("Access denied: You can only view your own documents.");
            }
        }

        return ResponseEntity.ok(document);
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentRequest documentRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        DocumentResponse created = documentService.createDocument(documentRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest documentRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        DocumentResponse updated = documentService.updateDocument(id, documentRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
