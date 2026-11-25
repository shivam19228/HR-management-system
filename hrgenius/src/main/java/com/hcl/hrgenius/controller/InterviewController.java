package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.interview.InterviewRequest;
import com.hcl.hrgenius.dto.interview.InterviewResponse;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.InterviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final SecurityHelper securityHelper;

    public InterviewController(InterviewService interviewService, SecurityHelper securityHelper) {
        this.interviewService = interviewService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getInterviews(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(interviewService.getInterviews(search, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getInterviewById(id));
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> createInterview(@Valid @RequestBody InterviewRequest interviewRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        InterviewResponse created = interviewService.createInterview(interviewRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest interviewRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        InterviewResponse updated = interviewService.updateInterview(id, interviewRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        interviewService.deleteInterview(id);
        return ResponseEntity.noContent().build();
    }
}
