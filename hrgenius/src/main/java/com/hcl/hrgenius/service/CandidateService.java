package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.candidate.CandidateRequest;
import com.hcl.hrgenius.dto.candidate.CandidateResponse;
import com.hcl.hrgenius.entity.Candidate;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> getCandidates(String search, String status) {
        List<Candidate> candidates;
        if ((search == null || search.trim().isEmpty()) && (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))) {
            candidates = candidateRepository.findAll();
        } else {
            candidates = candidateRepository.searchCandidates(search, status);
        }
        return candidates.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
        return mapToResponse(candidate);
    }

    public CandidateResponse createCandidate(CandidateRequest request) {
        Candidate candidate = new Candidate();
        String generatedCandidateId = request.getCandidateId();
        if (generatedCandidateId == null || generatedCandidateId.trim().isEmpty()) {
            generatedCandidateId = "CAN-" + (System.currentTimeMillis() % 100000);
        }
        candidate.setCandidateId(generatedCandidateId);

        String name = request.getName();
        if ((name == null || name.trim().isEmpty()) && (request.getFirstName() != null || request.getLastName() != null)) {
            name = (request.getFirstName() != null ? request.getFirstName() : "") + " " + (request.getLastName() != null ? request.getLastName() : "");
            name = name.trim();
        }
        candidate.setName(name != null ? name : "New Candidate");
        candidate.setFirstName(request.getFirstName() != null ? request.getFirstName() : "");
        candidate.setLastName(request.getLastName() != null ? request.getLastName() : "");
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setAppliedJob(request.getAppliedJob());
        candidate.setStatus(request.getStatus() != null ? request.getStatus() : "Applied");
        candidate.setQualification(request.getQualification());
        candidate.setExperience(request.getExperience());
        candidate.setResumeUrl(request.getResumeUrl());
        candidate.setApplicationDate(request.getApplicationDate() != null ? request.getApplicationDate() : LocalDate.now());

        Candidate saved = candidateRepository.save(candidate);
        return mapToResponse(saved);
    }

    public CandidateResponse updateCandidate(Long id, CandidateRequest request) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));

        if (request.getCandidateId() != null && !request.getCandidateId().trim().isEmpty()) {
            candidate.setCandidateId(request.getCandidateId());
        }
        if (request.getName() != null) candidate.setName(request.getName());
        if (request.getFirstName() != null) candidate.setFirstName(request.getFirstName());
        if (request.getLastName() != null) candidate.setLastName(request.getLastName());
        if (request.getEmail() != null) candidate.setEmail(request.getEmail());
        if (request.getPhone() != null) candidate.setPhone(request.getPhone());
        if (request.getAppliedJob() != null) candidate.setAppliedJob(request.getAppliedJob());
        if (request.getStatus() != null) candidate.setStatus(request.getStatus());
        if (request.getQualification() != null) candidate.setQualification(request.getQualification());
        if (request.getExperience() != null) candidate.setExperience(request.getExperience());
        if (request.getResumeUrl() != null) candidate.setResumeUrl(request.getResumeUrl());
        if (request.getApplicationDate() != null) candidate.setApplicationDate(request.getApplicationDate());

        Candidate updated = candidateRepository.save(candidate);
        return mapToResponse(updated);
    }

    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidate not found with id: " + id);
        }
        candidateRepository.deleteById(id);
    }

    private CandidateResponse mapToResponse(Candidate candidate) {
        CandidateResponse resp = new CandidateResponse();
        resp.setId(candidate.getId());
        resp.setCandidateId(candidate.getCandidateId());
        resp.setName(candidate.getName());
        resp.setFirstName(candidate.getFirstName());
        resp.setLastName(candidate.getLastName());
        resp.setEmail(candidate.getEmail());
        resp.setPhone(candidate.getPhone());
        resp.setAppliedJob(candidate.getAppliedJob());
        resp.setStatus(candidate.getStatus());
        resp.setQualification(candidate.getQualification());
        resp.setExperience(candidate.getExperience());
        resp.setResumeUrl(candidate.getResumeUrl());
        resp.setApplicationDate(candidate.getApplicationDate());
        return resp;
    }
}
