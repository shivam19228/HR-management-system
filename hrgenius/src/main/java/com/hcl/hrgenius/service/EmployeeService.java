package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.employee.EmployeeRequest;
import com.hcl.hrgenius.dto.employee.EmployeeResponse;
import com.hcl.hrgenius.entity.Department;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.DepartmentRepository;
import com.hcl.hrgenius.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.hcl.hrgenius.repository.UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<EmployeeResponse> getEmployees(String search, String department, Long departmentId, String status) {
        Long deptId = departmentId;
        if (deptId == null && department != null && !department.isBlank() && !"All".equalsIgnoreCase(department)) {
            try {
                deptId = Long.parseLong(department.trim());
            } catch (NumberFormatException ignored) {
                // Non-numeric string, handled as department name, code, or business ID
            }
        }

        boolean hasFilter = (search != null && !search.isBlank())
                || (department != null && !department.isBlank() && !"All".equalsIgnoreCase(department))
                || (deptId != null)
                || (status != null && !status.isBlank() && !"All".equalsIgnoreCase(status));

        List<Employee> employees;
        if (hasFilter) {
            employees = employeeRepository.searchEmployees(
                    search != null ? search.trim() : null,
                    department != null && !"All".equalsIgnoreCase(department) ? department.trim() : null,
                    deptId,
                    status != null && !"All".equalsIgnoreCase(status) ? status.trim() : null
            );
        } else {
            employees = employeeRepository.findAllWithDepartment();
        }

        return employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployees(String search, String department, String status) {
        return getEmployees(search, department, null, status);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request.getEmail() != null && employeeRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("An employee with email '" + request.getEmail() + "' already exists.");
        }

        String empId = request.getEmpId();
        if (empId == null || empId.isBlank()) {
            long count = employeeRepository.count();
            empId = String.format("EMP-%03d", count + 1);
        } else if (employeeRepository.existsByEmpId(empId.trim())) {
            throw new IllegalArgumentException("An employee with ID '" + empId + "' already exists.");
        }

        Employee employee = new Employee();
        employee.setEmpId(empId.trim());
        employee.setName(request.getName().trim());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail().trim());
        employee.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        employee.setJob(request.getJob());
        employee.setDesignation(request.getDesignation() != null ? request.getDesignation() : request.getJob());
        employee.setJoiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now());
        employee.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "Active");
        employee.setSalary(request.getSalary());
        employee.setGender(request.getGender());
        employee.setEmploymentType(request.getEmploymentType() != null ? request.getEmploymentType() : "Full Time");
        employee.setCity(request.getCity());
        employee.setAddress(request.getAddress());

        // Resolve Department by ID or Name
        Department department = resolveDepartment(request.getDepartmentId(), request.getDepartment());
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);

        // Synchronize / auto-provision User login credentials for newly created employee
        if (userRepository != null && !userRepository.existsByEmail(saved.getEmail())) {
            String baseUser = saved.getEmail().contains("@")
                    ? saved.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9_]", "_")
                    : saved.getEmpId().toLowerCase();
            String username = baseUser;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUser + counter++;
            }
            String rawPass = "Password@123";
            String encPass = passwordEncoder != null ? passwordEncoder.encode(rawPass) : rawPass;
            com.hcl.hrgenius.entity.User user = new com.hcl.hrgenius.entity.User(
                    username,
                    encPass,
                    saved.getEmail(),
                    saved.getName(),
                    "Employee",
                    saved.getStatus() != null ? saved.getStatus() : "Active"
            );
            userRepository.save(user);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        String oldEmail = employee.getEmail();
        String oldName = employee.getName();
        String newEmail = request.getEmail().trim();
        if (!employee.getEmail().equalsIgnoreCase(newEmail) && employeeRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("An employee with email '" + newEmail + "' already exists.");
        }

        if (request.getEmpId() != null && !request.getEmpId().isBlank()
                && !employee.getEmpId().equalsIgnoreCase(request.getEmpId().trim())
                && employeeRepository.existsByEmpId(request.getEmpId().trim())) {
            throw new IllegalArgumentException("An employee with ID '" + request.getEmpId() + "' already exists.");
        }

        if (request.getEmpId() != null && !request.getEmpId().isBlank()) {
            employee.setEmpId(request.getEmpId().trim());
        }
        employee.setName(request.getName().trim());
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            employee.setFirstName(request.getFirstName());
        } else if (employee.getFirstName() == null && request.getName() != null) {
            String[] parts = request.getName().trim().split("\\s+", 2);
            employee.setFirstName(parts[0]);
            if (parts.length > 1 && employee.getLastName() == null) {
                employee.setLastName(parts[1]);
            }
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            employee.setLastName(request.getLastName());
        }
        employee.setEmail(newEmail);
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            employee.setPhone(request.getPhone());
        }
        if (request.getJob() != null && !request.getJob().isBlank()) {
            employee.setJob(request.getJob());
        }
        if (request.getDesignation() != null && !request.getDesignation().isBlank()) {
            employee.setDesignation(request.getDesignation());
            if (employee.getJob() == null) {
                employee.setJob(request.getDesignation());
            }
        } else if (request.getJob() != null && !request.getJob().isBlank()) {
            employee.setDesignation(request.getJob());
        }
        if (request.getJoiningDate() != null) {
            employee.setJoiningDate(request.getJoiningDate());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            employee.setStatus(request.getStatus());
        }
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        if (request.getGender() != null && !request.getGender().isBlank()) {
            employee.setGender(request.getGender());
        }
        if (request.getEmploymentType() != null && !request.getEmploymentType().isBlank()) {
            employee.setEmploymentType(request.getEmploymentType());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            employee.setCity(request.getCity());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            employee.setAddress(request.getAddress());
        }

        // Resolve Department by ID or Name
        Department department = resolveDepartment(request.getDepartmentId(), request.getDepartment());
        employee.setDepartment(department);

        Employee updated = employeeRepository.save(employee);

        // Synchronize linked User login credentials
        if (userRepository != null) {
            java.util.Optional<com.hcl.hrgenius.entity.User> userOpt = userRepository.findByEmailIgnoreCase(oldEmail);
            if (userOpt.isEmpty() && oldName != null) {
                userOpt = userRepository.findByUsernameOrEmail(oldName, oldName);
            }
            if (userOpt.isPresent()) {
                com.hcl.hrgenius.entity.User u = userOpt.get();
                u.setEmail(updated.getEmail());
                u.setEmployee(updated.getName());
                if (updated.getStatus() != null) {
                    u.setStatus(updated.getStatus());
                }
                userRepository.save(u);
            }
        }

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Also clean up linked User account if present
        if (userRepository != null && employee.getEmail() != null) {
            userRepository.findByEmailIgnoreCase(employee.getEmail().trim())
                    .or(() -> userRepository.findByUsername(employee.getEmail().trim()))
                    .ifPresent(userRepository::delete);
        }

        employeeRepository.delete(employee);
    }

    private Department resolveDepartment(Long departmentId, String departmentName) {
        if (departmentName != null && !departmentName.isBlank()) {
            String nameTrimmed = departmentName.trim();
            Optional<Department> dept = departmentRepository.findByNameIgnoreCase(nameTrimmed);
            if (dept.isPresent()) {
                return dept.get();
            }
            dept = departmentRepository.findByDepartmentId(nameTrimmed);
            if (dept.isPresent()) {
                return dept.get();
            }
            dept = departmentRepository.findByCodeIgnoreCase(nameTrimmed);
            if (dept.isPresent()) {
                return dept.get();
            }
            try {
                long parsedId = Long.parseLong(nameTrimmed);
                dept = departmentRepository.findById(parsedId);
                if (dept.isPresent()) {
                    return dept.get();
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (departmentId != null) {
            return departmentRepository.findById(departmentId).orElse(null);
        }
        return null;
    }

    public EmployeeResponse mapToResponse(Employee emp) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(emp.getId());
        response.setEmpId(emp.getEmpId());
        response.setName(emp.getName());
        response.setFirstName(emp.getFirstName());
        response.setLastName(emp.getLastName());
        response.setEmail(emp.getEmail());
        response.setPhone(emp.getPhone());
        response.setJob(emp.getJob());
        response.setDesignation(emp.getDesignation());
        response.setJoiningDate(emp.getJoiningDate());
        response.setStatus(emp.getStatus());
        response.setSalary(emp.getSalary());
        response.setGender(emp.getGender());
        response.setEmploymentType(emp.getEmploymentType());
        response.setCity(emp.getCity());
        response.setAddress(emp.getAddress());

        if (emp.getDepartment() != null) {
            response.setDepartment(emp.getDepartment().getName());
            response.setDepartmentId(emp.getDepartment().getId());
        } else {
            response.setDepartment("Unassigned");
            response.setDepartmentId(null);
        }

        return response;
    }
}
