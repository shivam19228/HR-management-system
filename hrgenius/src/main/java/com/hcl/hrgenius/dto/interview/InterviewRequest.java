package com.hcl.hrgenius.dto.interview;

import jakarta.validation.constraints.NotBlank;

public class InterviewRequest {

    @NotBlank(message = "Candidate name is required")
    private String candidate;

    @NotBlank(message = "Job title is required")
    private String job;

    private String interviewDate;

    @NotBlank(message = "Interviewer is required")
    private String interviewer;

    private String mode = "Online";

    private String status = "Scheduled";

    private String feedback;

    public InterviewRequest() {
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
