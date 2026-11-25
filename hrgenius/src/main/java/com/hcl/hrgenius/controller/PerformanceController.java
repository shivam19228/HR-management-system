package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.performance.PerformanceRequest;
import com.hcl.hrgenius.dto.performance.PerformanceResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.PerformanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final SecurityHelper securityHelper;

    public PerformanceController(PerformanceService performanceService, SecurityHelper securityHelper) {
        this.performanceService = performanceService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<PerformanceResponse>> getReviews(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minRating,
            HttpServletRequest request) {

        List<PerformanceResponse> reviews = performanceService.getReviews(search, minRating);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            Employee emp = empOpt.get();
            List<PerformanceResponse> ownReviews = reviews.stream()
                    .filter(r -> (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(r.getEmployeeId()))
                            || (emp.getName() != null && emp.getName().equalsIgnoreCase(r.getEmployee())))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownReviews);
        }

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceResponse> getReviewById(@PathVariable Long id, HttpServletRequest request) {
        PerformanceResponse review = performanceService.getReviewById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                throw new ForbiddenException("Access denied: You can only view your own review.");
            }
            Employee emp = empOpt.get();
            boolean isOwn = (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(review.getEmployeeId()))
                    || (emp.getName() != null && emp.getName().equalsIgnoreCase(review.getEmployee()));
            if (!isOwn) {
                throw new ForbiddenException("Access denied: You can only view your own review.");
            }
        }

        return ResponseEntity.ok(review);
    }

    @PostMapping
    public ResponseEntity<PerformanceResponse> createReview(@Valid @RequestBody PerformanceRequest performanceRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        PerformanceResponse created = performanceService.createReview(performanceRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformanceResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody PerformanceRequest performanceRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        PerformanceResponse updated = performanceService.updateReview(id, performanceRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        performanceService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
