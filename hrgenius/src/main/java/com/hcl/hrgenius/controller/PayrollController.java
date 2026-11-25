package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.payroll.PayrollRequest;
import com.hcl.hrgenius.dto.payroll.PayrollResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.PayrollService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final SecurityHelper securityHelper;

    public PayrollController(PayrollService payrollService, SecurityHelper securityHelper) {
        this.payrollService = payrollService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<PayrollResponse>> getPayrolls(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        List<PayrollResponse> payrolls = payrollService.getPayrolls(search, status);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            Employee emp = empOpt.get();
            List<PayrollResponse> ownPayrolls = payrolls.stream()
                    .filter(p -> (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(p.getEmployeeId()))
                            || (emp.getName() != null && emp.getName().equalsIgnoreCase(p.getEmployee())))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownPayrolls);
        }

        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollResponse> getPayrollById(@PathVariable Long id, HttpServletRequest request) {
        PayrollResponse payroll = payrollService.getPayrollById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                throw new ForbiddenException("Access denied: You are only authorized to view your own payroll record.");
            }
            Employee emp = empOpt.get();
            boolean isOwn = (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(payroll.getEmployeeId()))
                    || (emp.getName() != null && emp.getName().equalsIgnoreCase(payroll.getEmployee()));
            if (!isOwn) {
                throw new ForbiddenException("Access denied: You are only authorized to view your own payroll record.");
            }
        }

        return ResponseEntity.ok(payroll);
    }

    @PostMapping
    public ResponseEntity<PayrollResponse> createPayroll(@Valid @RequestBody PayrollRequest payrollRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        PayrollResponse created = payrollService.createPayroll(payrollRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollResponse> updatePayroll(
            @PathVariable Long id,
            @Valid @RequestBody PayrollRequest payrollRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        PayrollResponse updated = payrollService.updatePayroll(id, payrollRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayroll(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        payrollService.deletePayroll(id);
        return ResponseEntity.noContent().build();
    }
}
