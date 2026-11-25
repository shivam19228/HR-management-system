package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.employee.EmployeeRequest;
import com.hcl.hrgenius.dto.employee.EmployeeResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SecurityHelper securityHelper;

    public EmployeeController(EmployeeService employeeService, SecurityHelper securityHelper) {
        this.employeeService = employeeService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        List<EmployeeResponse> employees = employeeService.getEmployees(search, department, departmentId, status);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> currentEmpOpt = securityHelper.getEmployeeForRequest(request);
            String currentEmpId = currentEmpOpt.map(Employee::getEmpId).orElse("");
            for (EmployeeResponse emp : employees) {
                if (!currentEmpId.equalsIgnoreCase(emp.getEmpId())) {
                    emp.setSalary(null);
                }
            }
        }

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id, HttpServletRequest request) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> currentEmpOpt = securityHelper.getEmployeeForRequest(request);
            String currentEmpId = currentEmpOpt.map(Employee::getEmpId).orElse("");
            if (!currentEmpId.equalsIgnoreCase(employee.getEmpId())) {
                employee.setSalary(null);
            }
        }

        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest employeeRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        EmployeeResponse created = employeeService.createEmployee(employeeRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest employeeRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        EmployeeResponse updated = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
