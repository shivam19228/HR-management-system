package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.candidate.CandidateRequest;
import com.hcl.hrgenius.dto.candidate.CandidateResponse;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.CandidateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;
    private final SecurityHelper securityHelper;

    public CandidateController(CandidateService candidateService, SecurityHelper securityHelper) {
        this.candidateService = candidateService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getCandidates(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(candidateService.getCandidates(search, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> createCandidate(@Valid @RequestBody CandidateRequest candidateRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        CandidateResponse created = candidateService.createCandidate(candidateRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> updateCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequest candidateRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        CandidateResponse updated = candidateService.updateCandidate(id, candidateRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }
}
