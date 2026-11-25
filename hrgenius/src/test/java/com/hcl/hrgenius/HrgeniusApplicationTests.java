package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.employee.EmployeeResponse;
import com.hcl.hrgenius.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HrgeniusApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void contextLoads() {
    }

    @Test
    void testFilterByDepartmentId_Param() {
        // Regression test for GET /api/employees?departmentId=2
        List<EmployeeResponse> list = employeeService.getEmployees(null, null, 2L, null);
        assertNotNull(list);
        assertTrue(list.size() >= 2, "Filtering by departmentId=2 should return matching employees");
        assertTrue(list.stream().allMatch(e -> Long.valueOf(2L).equals(e.getDepartmentId())));
        assertTrue(list.stream().anyMatch(e -> "David Chen".equals(e.getName())));
        assertTrue(list.stream().anyMatch(e -> "Jessica Taylor".equals(e.getName())));
    }

    @Test
    void testFilterByDepartment_NumericString() {
        // Regression test for GET /api/employees?department=2
        List<EmployeeResponse> list = employeeService.getEmployees(null, "2", null, null);
        assertNotNull(list);
        assertTrue(list.size() >= 2, "Filtering by department=2 should return matching employees");
        assertTrue(list.stream().allMatch(e -> Long.valueOf(2L).equals(e.getDepartmentId())));
    }

    @Test
    void testFilterByDepartment_NameString() {
        // Regression test for GET /api/employees?department=Software%20Engineering
        List<EmployeeResponse> list = employeeService.getEmployees(null, "Software Engineering", null, null);
        assertNotNull(list);
        assertTrue(list.size() >= 2, "Filtering by department=Software Engineering should return matching employees");
        assertTrue(list.stream().allMatch(e -> "Software Engineering".equals(e.getDepartment())));
    }
}
