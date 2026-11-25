package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.dashboard.DashboardStatsResponse;
import com.hcl.hrgenius.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CandidateRepository candidateRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final JobRepository jobRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                            DepartmentRepository departmentRepository,
                            CandidateRepository candidateRepository,
                            AttendanceRepository attendanceRepository,
                            LeaveRequestRepository leaveRepository,
                            PayrollRepository payrollRepository,
                            JobRepository jobRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.candidateRepository = candidateRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.payrollRepository = payrollRepository;
        this.jobRepository = jobRepository;
    }

    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalEmployees(employeeRepository.count());
        stats.setTotalDepartments(departmentRepository.count());
        stats.setTotalCandidates(candidateRepository.count());
        stats.setPresentToday(attendanceRepository.countByStatus("Present") + attendanceRepository.countByStatus("Late"));
        stats.setPendingLeaves(leaveRepository.countByStatus("Pending"));
        stats.setTotalPayroll(payrollRepository.sumTotalPayroll());

        BigDecimal avgSal = employeeRepository.findAverageSalary();
        stats.setAvgSalary(avgSal != null ? avgSal.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        stats.setOpenJobs(jobRepository.countByStatus("Open"));
        return stats;
    }
}
