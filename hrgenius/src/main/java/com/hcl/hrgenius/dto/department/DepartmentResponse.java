package com.hcl.hrgenius.dto.department;

public class DepartmentResponse {

    private Long id;
    private String departmentId;
    private String name;
    private String code;
    private String description;
    private String manager;
    private int employeeCount;

    public DepartmentResponse() {
    }

    public DepartmentResponse(Long id, String departmentId, String name, String code, String description, String manager, int employeeCount) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.manager = manager;
        this.employeeCount = employeeCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }
}
