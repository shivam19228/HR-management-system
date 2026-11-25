package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.document.DocumentRequest;
import com.hcl.hrgenius.dto.document.DocumentResponse;
import com.hcl.hrgenius.entity.DocumentRecord;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(String search, String status, String type) {
        List<DocumentRecord> list;
        if ((search == null || search.trim().isEmpty()) &&
            (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status)) &&
            (type == null || type.trim().isEmpty() || "All".equalsIgnoreCase(type))) {
            list = documentRepository.findAll();
        } else {
            list = documentRepository.searchDocuments(search, status, type);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        DocumentRecord doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document record not found with id: " + id));
        return mapToResponse(doc);
    }

    public DocumentResponse createDocument(DocumentRequest request) {
        DocumentRecord doc = new DocumentRecord();
        doc.setEmployee(request.getEmployee());
        doc.setDocumentName(request.getDocumentName());
        doc.setDocumentType(request.getDocumentType() != null ? request.getDocumentType() : "Contract");
        doc.setDocumentUrl(request.getDocumentUrl() != null ? request.getDocumentUrl() : "/documents/default_document.pdf");
        doc.setUploadDate(request.getUploadDate() != null ? request.getUploadDate() : LocalDate.now());
        doc.setStatus(request.getStatus() != null ? request.getStatus() : "Pending");

        DocumentRecord saved = documentRepository.save(doc);
        return mapToResponse(saved);
    }

    public DocumentResponse updateDocument(Long id, DocumentRequest request) {
        DocumentRecord doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document record not found with id: " + id));

        if (request.getEmployee() != null) doc.setEmployee(request.getEmployee());
        if (request.getDocumentName() != null) doc.setDocumentName(request.getDocumentName());
        if (request.getDocumentType() != null) doc.setDocumentType(request.getDocumentType());
        if (request.getDocumentUrl() != null) doc.setDocumentUrl(request.getDocumentUrl());
        if (request.getUploadDate() != null) doc.setUploadDate(request.getUploadDate());
        if (request.getStatus() != null) doc.setStatus(request.getStatus());

        DocumentRecord updated = documentRepository.save(doc);
        return mapToResponse(updated);
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document record not found with id: " + id);
        }
        documentRepository.deleteById(id);
    }

    private DocumentResponse mapToResponse(DocumentRecord doc) {
        DocumentResponse resp = new DocumentResponse();
        resp.setId(doc.getId());
        resp.setEmployee(doc.getEmployee());
        resp.setDocumentName(doc.getDocumentName());
        resp.setDocumentType(doc.getDocumentType());
        resp.setDocumentUrl(doc.getDocumentUrl());
        resp.setUploadDate(doc.getUploadDate());
        resp.setStatus(doc.getStatus());
        return resp;
    }
}
