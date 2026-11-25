package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.job.JobRequest;
import com.hcl.hrgenius.dto.job.JobResponse;
import com.hcl.hrgenius.entity.Job;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJobs(String search, String status) {
        List<Job> jobs;
        if ((search == null || search.trim().isEmpty()) && (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))) {
            jobs = jobRepository.findAll();
        } else {
            jobs = jobRepository.searchJobs(search, status);
        }
        return jobs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));
        return mapToResponse(job);
    }

    public JobResponse createJob(JobRequest request) {
        Job job = new Job();
        String generatedJobId = request.getJobId();
        if (generatedJobId == null || generatedJobId.trim().isEmpty()) {
            generatedJobId = "JOB-" + (System.currentTimeMillis() % 100000);
        }
        job.setJobId(generatedJobId);
        job.setJobTitle(request.getJobTitle());
        job.setDepartment(request.getDepartment());
        job.setDescription(request.getDescription());
        job.setStatus(request.getStatus() != null ? request.getStatus() : "Open");
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType() != null ? request.getEmploymentType() : "Full Time");
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSalary(request.getSalary());

        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    public JobResponse updateJob(Long id, JobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));

        if (request.getJobId() != null && !request.getJobId().trim().isEmpty()) {
            job.setJobId(request.getJobId());
        }
        if (request.getJobTitle() != null) job.setJobTitle(request.getJobTitle());
        if (request.getDepartment() != null) job.setDepartment(request.getDepartment());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getStatus() != null) job.setStatus(request.getStatus());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getEmploymentType() != null) job.setEmploymentType(request.getEmploymentType());
        if (request.getExperienceRequired() != null) job.setExperienceRequired(request.getExperienceRequired());
        if (request.getSalary() != null) job.setSalary(request.getSalary());

        Job updated = jobRepository.save(job);
        return mapToResponse(updated);
    }

    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job opening not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setJobId(job.getJobId());
        resp.setJobTitle(job.getJobTitle());
        resp.setDepartment(job.getDepartment());
        resp.setDescription(job.getDescription());
        resp.setStatus(job.getStatus());
        resp.setLocation(job.getLocation());
        resp.setEmploymentType(job.getEmploymentType());
        resp.setExperienceRequired(job.getExperienceRequired());
        resp.setSalary(job.getSalary());
        return resp;
    }
}
