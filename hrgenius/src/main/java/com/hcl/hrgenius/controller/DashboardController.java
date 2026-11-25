package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.dashboard.DashboardStatsResponse;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityHelper securityHelper;

    public DashboardController(DashboardService dashboardService, SecurityHelper securityHelper) {
        this.dashboardService = dashboardService;
        this.securityHelper = securityHelper;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(HttpServletRequest request) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        if (securityHelper.isEmployeeOnly(request)) {
            stats.setTotalPayroll(null);
            stats.setAvgSalary(null);
        }
        return ResponseEntity.ok(stats);
    }
}
