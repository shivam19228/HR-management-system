package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.attendance.AttendanceRequest;
import com.hcl.hrgenius.dto.attendance.AttendanceResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SecurityHelper securityHelper;

    public AttendanceController(AttendanceService attendanceService, SecurityHelper securityHelper) {
        this.attendanceService = attendanceService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {

        List<AttendanceResponse> list = attendanceService.getAttendance(search, status, date);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            Employee emp = empOpt.get();
            List<AttendanceResponse> ownList = list.stream()
                    .filter(a -> (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(a.getEmployeeId()))
                            || (emp.getName() != null && emp.getName().equalsIgnoreCase(a.getEmployee())))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownList);
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable Long id, HttpServletRequest request) {
        AttendanceResponse att = attendanceService.getAttendanceById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                throw new ForbiddenException("Access denied: You are only authorized to view your own attendance record.");
            }
            Employee emp = empOpt.get();
            boolean isOwn = (emp.getEmpId() != null && emp.getEmpId().equalsIgnoreCase(att.getEmployeeId()))
                    || (emp.getName() != null && emp.getName().equalsIgnoreCase(att.getEmployee()));
            if (!isOwn) {
                throw new ForbiddenException("Access denied: You are only authorized to view your own attendance record.");
            }
        }

        return ResponseEntity.ok(att);
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> createAttendance(@Valid @RequestBody AttendanceRequest attendanceRequest, HttpServletRequest request) {
        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                attendanceRequest.setEmployee(emp.getName());
                attendanceRequest.setEmployeeId(emp.getEmpId());
            }
        }
        AttendanceResponse created = attendanceService.createAttendance(attendanceRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest attendanceRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        AttendanceResponse updated = attendanceService.updateAttendance(id, attendanceRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}
