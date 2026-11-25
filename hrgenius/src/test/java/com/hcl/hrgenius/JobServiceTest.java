package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.job.JobRequest;
import com.hcl.hrgenius.dto.job.JobResponse;
import com.hcl.hrgenius.entity.Job;
import com.hcl.hrgenius.repository.JobRepository;
import com.hcl.hrgenius.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job sampleJob;

    @BeforeEach
    void setUp() {
        sampleJob = new Job("JOB-201", "Senior Full Stack Engineer", "Software Engineering",
                "Job description", "Open", "Remote", "Full Time", "4+ Years", "$100,000");
        sampleJob.setId(1L);
    }

    @Test
    void testGetJobs_ReturnsList() {
        when(jobRepository.findAll()).thenReturn(List.of(sampleJob));

        List<JobResponse> list = jobService.getJobs(null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Senior Full Stack Engineer", list.get(0).getJobTitle());
        assertEquals("Open", list.get(0).getStatus());
    }

    @Test
    void testCreateJob_Success() {
        JobRequest request = new JobRequest();
        request.setJobId("JOB-201");
        request.setJobTitle("Senior Full Stack Engineer");
        request.setDepartment("Software Engineering");
        request.setStatus("Open");

        when(jobRepository.save(any(Job.class))).thenReturn(sampleJob);

        JobResponse response = jobService.createJob(request);

        assertNotNull(response);
        assertEquals("JOB-201", response.getJobId());
        assertEquals("Senior Full Stack Engineer", response.getJobTitle());
    }

    @Test
    void testGetJobById_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));

        JobResponse response = jobService.getJobById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Software Engineering", response.getDepartment());
    }
}
