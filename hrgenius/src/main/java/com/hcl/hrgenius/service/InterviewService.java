package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.interview.InterviewRequest;
import com.hcl.hrgenius.dto.interview.InterviewResponse;
import com.hcl.hrgenius.entity.Interview;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;

    public InterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviews(String search, String status) {
        List<Interview> list;
        if ((search == null || search.trim().isEmpty()) && (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))) {
            list = interviewRepository.findAll();
        } else {
            list = interviewRepository.searchInterviews(search, status);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));
        return mapToResponse(interview);
    }

    public InterviewResponse createInterview(InterviewRequest request) {
        Interview interview = new Interview();
        interview.setCandidate(request.getCandidate());
        interview.setJob(request.getJob());
        interview.setInterviewDate(request.getInterviewDate());
        interview.setInterviewer(request.getInterviewer());
        interview.setMode(request.getMode() != null ? request.getMode() : "Online");
        interview.setStatus(request.getStatus() != null ? request.getStatus() : "Scheduled");
        interview.setFeedback(request.getFeedback());

        Interview saved = interviewRepository.save(interview);
        return mapToResponse(saved);
    }

    public InterviewResponse updateInterview(Long id, InterviewRequest request) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));

        if (request.getCandidate() != null) interview.setCandidate(request.getCandidate());
        if (request.getJob() != null) interview.setJob(request.getJob());
        if (request.getInterviewDate() != null) interview.setInterviewDate(request.getInterviewDate());
        if (request.getInterviewer() != null) interview.setInterviewer(request.getInterviewer());
        if (request.getMode() != null) interview.setMode(request.getMode());
        if (request.getStatus() != null) interview.setStatus(request.getStatus());
        if (request.getFeedback() != null) interview.setFeedback(request.getFeedback());

        Interview updated = interviewRepository.save(interview);
        return mapToResponse(updated);
    }

    public void deleteInterview(Long id) {
        if (!interviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interview not found with id: " + id);
        }
        interviewRepository.deleteById(id);
    }

    private InterviewResponse mapToResponse(Interview interview) {
        InterviewResponse resp = new InterviewResponse();
        resp.setId(interview.getId());
        resp.setCandidate(interview.getCandidate());
        resp.setJob(interview.getJob());
        resp.setInterviewDate(interview.getInterviewDate());
        resp.setInterviewer(interview.getInterviewer());
        resp.setMode(interview.getMode());
        resp.setStatus(interview.getStatus());
        resp.setFeedback(interview.getFeedback());
        return resp;
    }
}
