package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.job.JobRequest;
import com.hcl.hrgenius.dto.job.JobResponse;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final SecurityHelper securityHelper;

    public JobController(JobService jobService, SecurityHelper securityHelper) {
        this.jobService = jobService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(jobService.getJobs(search, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest jobRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        JobResponse created = jobService.createJob(jobRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest jobRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        JobResponse updated = jobService.updateJob(id, jobRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
