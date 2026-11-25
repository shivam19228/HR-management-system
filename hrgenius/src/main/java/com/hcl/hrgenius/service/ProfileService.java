package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.user.ProfileResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.entity.User;
import com.hcl.hrgenius.exception.UnauthorizedException;
import com.hcl.hrgenius.repository.EmployeeRepository;
import com.hcl.hrgenius.security.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final SecurityHelper securityHelper;
    private final EmployeeRepository employeeRepository;

    public ProfileService(SecurityHelper securityHelper, EmployeeRepository employeeRepository) {
        this.securityHelper = securityHelper;
        this.employeeRepository = employeeRepository;
    }

    public ProfileResponse getCurrentUserProfile(HttpServletRequest request) {
        User user = securityHelper.getAuthenticatedUser(request);
        if (user == null) {
            throw new UnauthorizedException("Authentication required. Please log in.");
        }

        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole() : "Employee");
        response.setStatus(user.getStatus() != null ? user.getStatus() : "Active");

        String name = user.getEmployee() != null && !user.getEmployee().isBlank()
                ? user.getEmployee()
                : user.getUsername();
        response.setName(name);

        String initials = "US";
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
            } else if (parts.length == 1 && !parts[0].isEmpty()) {
                initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
        }
        response.setAvatar(initials);

        Optional<Employee> empOpt = securityHelper.getAssociatedEmployee(user);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            response.setEmpId(emp.getEmpId());
            response.setEmployeeId(emp.getId());
            if (emp.getName() != null && !emp.getName().isBlank()) {
                response.setName(emp.getName());
            }
            if (emp.getEmail() != null && !emp.getEmail().isBlank()) {
                response.setEmail(emp.getEmail());
            }
            if (emp.getDepartment() != null) {
                response.setDepartment(emp.getDepartment().getName());
                response.setDepartmentId(emp.getDepartment().getId());
            } else {
                response.setDepartment("Unassigned");
            }
            response.setJob(emp.getJob() != null ? emp.getJob() : emp.getDesignation());
            response.setDesignation(emp.getDesignation() != null ? emp.getDesignation() : emp.getJob());
            response.setPhone(emp.getPhone());
            response.setJoiningDate(emp.getJoiningDate());
            response.setSalary(emp.getSalary());
            response.setGender(emp.getGender());
            response.setEmploymentType(emp.getEmploymentType() != null ? emp.getEmploymentType() : "Full Time");
            response.setCity(emp.getCity());
            response.setAddress(emp.getAddress());
            if (emp.getStatus() != null && !emp.getStatus().isBlank()) {
                response.setStatus(emp.getStatus());
            }
        } else {
            String roleUpper = user.getRole() != null ? user.getRole().toUpperCase() : "EMPLOYEE";
            if (roleUpper.contains("ADMIN")) {
                response.setEmpId("ADM-" + String.format("%03d", user.getId()));
                response.setDepartment("System Administration");
                response.setJob("System Administrator");
                response.setDesignation("Lead Administrator");
                response.setEmploymentType("Full Time");
            } else if (roleUpper.contains("MANAGE") || roleUpper.contains("LEAD")) {
                response.setEmpId("MGR-" + String.format("%03d", user.getId()));
                response.setDepartment("Human Resources");
                response.setJob("HR Manager");
                response.setDesignation("People Operations Lead");
                response.setEmploymentType("Full Time");
            } else {
                response.setEmpId("EMP-" + String.format("%03d", user.getId()));
                response.setDepartment("General Operations");
                response.setJob("Staff Associate");
                response.setDesignation("Staff Associate");
                response.setEmploymentType("Full Time");
            }
        }

        return response;
    }
}
