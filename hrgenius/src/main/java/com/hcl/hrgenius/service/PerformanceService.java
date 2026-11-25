package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.performance.PerformanceRequest;
import com.hcl.hrgenius.dto.performance.PerformanceResponse;
import com.hcl.hrgenius.entity.PerformanceReview;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.PerformanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    public PerformanceService(PerformanceRepository performanceRepository) {
        this.performanceRepository = performanceRepository;
    }

    @Transactional(readOnly = true)
    public List<PerformanceResponse> getReviews(String search, Integer minRating) {
        List<PerformanceReview> list;
        if ((search == null || search.trim().isEmpty()) && minRating == null) {
            list = performanceRepository.findAll();
        } else {
            list = performanceRepository.searchReviews(search, minRating);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PerformanceResponse getReviewById(Long id) {
        PerformanceReview review = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with id: " + id));
        return mapToResponse(review);
    }

    public PerformanceResponse createReview(PerformanceRequest request) {
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(request.getEmployee());
        review.setEmployeeId(request.getEmployeeId());
        review.setReviewDate(request.getReviewDate() != null ? request.getReviewDate() : LocalDate.now());
        review.setRating(request.getRating() != null ? request.getRating() : 5);
        review.setGoals(request.getGoals());
        review.setAchievements(request.getAchievements());
        review.setFeedback(request.getFeedback());

        PerformanceReview saved = performanceRepository.save(review);
        return mapToResponse(saved);
    }

    public PerformanceResponse updateReview(Long id, PerformanceRequest request) {
        PerformanceReview review = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with id: " + id));

        if (request.getEmployee() != null) review.setEmployee(request.getEmployee());
        if (request.getEmployeeId() != null) review.setEmployeeId(request.getEmployeeId());
        if (request.getReviewDate() != null) review.setReviewDate(request.getReviewDate());
        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getGoals() != null) review.setGoals(request.getGoals());
        if (request.getAchievements() != null) review.setAchievements(request.getAchievements());
        if (request.getFeedback() != null) review.setFeedback(request.getFeedback());

        PerformanceReview updated = performanceRepository.save(review);
        return mapToResponse(updated);
    }

    public void deleteReview(Long id) {
        if (!performanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Performance review not found with id: " + id);
        }
        performanceRepository.deleteById(id);
    }

    private PerformanceResponse mapToResponse(PerformanceReview review) {
        PerformanceResponse resp = new PerformanceResponse();
        resp.setId(review.getId());
        resp.setEmployee(review.getEmployee());
        resp.setEmployeeId(review.getEmployeeId());
        resp.setReviewDate(review.getReviewDate());
        resp.setRating(review.getRating());
        resp.setGoals(review.getGoals());
        resp.setAchievements(review.getAchievements());
        resp.setFeedback(review.getFeedback());
        return resp;
    }
}
