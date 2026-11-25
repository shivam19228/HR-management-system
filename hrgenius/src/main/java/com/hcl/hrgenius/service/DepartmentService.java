package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.department.DepartmentRequest;
import com.hcl.hrgenius.dto.department.DepartmentResponse;
import com.hcl.hrgenius.entity.Department;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAllWithEmployees()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new IllegalArgumentException("A department with name '" + request.getName() + "' already exists.");
        }
        if (departmentRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new IllegalArgumentException("A department with code '" + request.getCode() + "' already exists.");
        }

        String deptId = request.getDepartmentId();
        if (deptId == null || deptId.isBlank()) {
            long count = departmentRepository.count();
            deptId = String.format("DEP-%03d", count + 101);
        }

        Department department = new Department(
                deptId,
                request.getName().trim(),
                request.getCode().trim().toUpperCase(),
                request.getDescription(),
                request.getManager()
        );

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        String newName = request.getName().trim();
        if (!department.getName().equalsIgnoreCase(newName) && departmentRepository.existsByNameIgnoreCase(newName)) {
            throw new IllegalArgumentException("A department with name '" + newName + "' already exists.");
        }

        String newCode = request.getCode().trim().toUpperCase();
        if (!department.getCode().equalsIgnoreCase(newCode) && departmentRepository.existsByCodeIgnoreCase(newCode)) {
            throw new IllegalArgumentException("A department with code '" + newCode + "' already exists.");
        }

        department.setName(newName);
        department.setCode(newCode);
        department.setDescription(request.getDescription());
        department.setManager(request.getManager());
        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            department.setDepartmentId(request.getDepartmentId());
        }

        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Unlink any employees assigned to this department before deletion
        if (department.getEmployees() != null) {
            department.getEmployees().forEach(emp -> emp.setDepartment(null));
        }

        departmentRepository.delete(department);
    }

    public DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getManager(),
                department.getEmployeeCount()
        );
    }
}
