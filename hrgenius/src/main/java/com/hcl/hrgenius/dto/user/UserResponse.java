package com.hcl.hrgenius.dto.user;

public class UserResponse {

    private Long id;
    private String username;
    private String employee;
    private String email;
    private String role;
    private String status;
    private String empId;
    private Long employeeId;
    private String token;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String employee, String email, String role, String status) {
        this.id = id;
        this.username = username;
        this.employee = employee;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public UserResponse(Long id, String username, String employee, String email, String role, String status, String empId, Long employeeId) {
        this.id = id;
        this.username = username;
        this.employee = employee;
        this.email = email;
        this.role = role;
        this.status = status;
        this.empId = empId;
        this.employeeId = employeeId;
    }

    public UserResponse(Long id, String username, String employee, String email, String role, String status, String empId, Long employeeId, String token) {
        this.id = id;
        this.username = username;
        this.employee = employee;
        this.email = email;
        this.role = role;
        this.status = status;
        this.empId = empId;
        this.employeeId = employeeId;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}
