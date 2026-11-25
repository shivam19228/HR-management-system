package com.hcl.hrgenius.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentRequest {

    private String departmentId;

    @NotBlank(message = "Department name is required")
    @Size(max = 150, message = "Department name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Department code must not exceed 50 characters")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 150, message = "Manager name must not exceed 150 characters")
    private String manager;

    public DepartmentRequest() {
    }

    public DepartmentRequest(String departmentId, String name, String code, String description, String manager) {
        this.departmentId = departmentId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.manager = manager;
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
}
