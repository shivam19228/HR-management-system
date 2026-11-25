package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.leave.LeaveRequestDto;
import com.hcl.hrgenius.dto.leave.LeaveResponseDto;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;
    private final SecurityHelper securityHelper;

    public LeaveController(LeaveService leaveService, SecurityHelper securityHelper) {
        this.leaveService = leaveService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponseDto>> getLeaves(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        List<LeaveResponseDto> leaves = leaveService.getLeaves(search, status);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            Employee emp = empOpt.get();
            List<LeaveResponseDto> ownLeaves = leaves.stream()
                    .filter(l -> (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(l.getEmployeeId()))
                            || (emp.getName() != null && emp.getName().equalsIgnoreCase(l.getEmployee())))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownLeaves);
        }

        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> getLeaveById(@PathVariable Long id, HttpServletRequest request) {
        LeaveResponseDto leave = leaveService.getLeaveById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                throw new ForbiddenException("Access denied: You are only authorized to view your own leave requests.");
            }
            Employee emp = empOpt.get();
            boolean isOwn = (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(leave.getEmployeeId()))
                    || (emp.getName() != null && emp.getName().equalsIgnoreCase(leave.getEmployee()));
            if (!isOwn) {
                throw new ForbiddenException("Access denied: You are only authorized to view your own leave requests.");
            }
        }

        return ResponseEntity.ok(leave);
    }

    @PostMapping
    public ResponseEntity<LeaveResponseDto> createLeave(@Valid @RequestBody LeaveRequestDto leaveRequest, HttpServletRequest request) {
        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                leaveRequest.setEmployee(emp.getName());
                leaveRequest.setEmployeeId(emp.getEmpId());
            }
            leaveRequest.setStatus("Pending");
        }
        LeaveResponseDto created = leaveService.createLeave(leaveRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDto leaveRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        LeaveResponseDto updated = leaveService.updateLeave(id, leaveRequest);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveResponseDto> updateLeaveStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        String status = body != null ? body.get("status") : null;
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        LeaveResponseDto updated = leaveService.updateLeaveStatus(id, status.trim());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id, HttpServletRequest request) {
        if (securityHelper.isEmployeeOnly(request)) {
            LeaveResponseDto leave = leaveService.getLeaveById(id);
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty() || !empOpt.get().getEmpId().equalsIgnoreCase(leave.getEmployeeId())) {
                throw new ForbiddenException("Access denied: You can only delete your own leave request.");
            }
        }
        leaveService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}
