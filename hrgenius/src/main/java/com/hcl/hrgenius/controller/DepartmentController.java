package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.department.DepartmentRequest;
import com.hcl.hrgenius.dto.department.DepartmentResponse;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.DepartmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final SecurityHelper securityHelper;

    public DepartmentController(DepartmentService departmentService, SecurityHelper securityHelper) {
        this.departmentService = departmentService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest departmentRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        DepartmentResponse created = departmentService.createDepartment(departmentRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest departmentRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        DepartmentResponse updated = departmentService.updateDepartment(id, departmentRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
