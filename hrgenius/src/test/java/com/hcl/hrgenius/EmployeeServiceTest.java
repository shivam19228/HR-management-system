package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.employee.EmployeeRequest;
import com.hcl.hrgenius.dto.employee.EmployeeResponse;
import com.hcl.hrgenius.entity.Department;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.repository.DepartmentRepository;
import com.hcl.hrgenius.repository.EmployeeRepository;
import com.hcl.hrgenius.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = new Department("DEP-102", "Software Engineering", "ENG-02", "Dev", "David Chen");
        department.setId(2L);

        employee = new Employee();
        employee.setId(1L);
        employee.setEmpId("EMP-001");
        employee.setName("Sarah Jenkins");
        employee.setEmail("sarah@hrgenius.com");
        employee.setDepartment(department);
        employee.setSalary(new BigDecimal("85000.00"));
        employee.setJoiningDate(LocalDate.of(2022, 3, 15));
        employee.setStatus("Active");
    }

    @Test
    void testGetEmployees_WithoutFilters() {
        when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee));

        List<EmployeeResponse> list = employeeService.getEmployees(null, null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Sarah Jenkins", list.get(0).getName());
        assertEquals("Software Engineering", list.get(0).getDepartment());
        verify(employeeRepository, times(1)).findAllWithDepartment();
    }

    @Test
    void testGetEmployees_WithDepartmentName() {
        when(employeeRepository.searchEmployees(null, "Software Engineering", null, null))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> list = employeeService.getEmployees(null, "Software Engineering", null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Software Engineering", list.get(0).getDepartment());
        verify(employeeRepository, times(1)).searchEmployees(null, "Software Engineering", null, null);
    }

    @Test
    void testGetEmployees_WithDepartmentIdNumericString() {
        when(employeeRepository.searchEmployees(null, "2", 2L, null))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> list = employeeService.getEmployees(null, "2", null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Software Engineering", list.get(0).getDepartment());
        assertEquals(2L, list.get(0).getDepartmentId());
        verify(employeeRepository, times(1)).searchEmployees(null, "2", 2L, null);
    }

    @Test
    void testGetEmployees_WithDepartmentIdParam() {
        when(employeeRepository.searchEmployees(null, null, 2L, null))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> list = employeeService.getEmployees(null, null, 2L, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(2L, list.get(0).getDepartmentId());
        verify(employeeRepository, times(1)).searchEmployees(null, null, 2L, null);
    }

    @Test
    void testGetEmployees_WithSearchAndFilter() {
        when(employeeRepository.searchEmployees("Sarah", "Software Engineering", null, "Active"))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> list = employeeService.getEmployees("Sarah", "Software Engineering", "Active");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Sarah Jenkins", list.get(0).getName());
        verify(employeeRepository, times(1)).searchEmployees("Sarah", "Software Engineering", null, "Active");
    }

    @Test
    void testCreateEmployee_Success() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Sarah Jenkins");
        request.setEmail("sarah.new@hrgenius.com");
        request.setPhone("+1 555-0101");
        request.setDepartment("Software Engineering");
        request.setSalary(new BigDecimal("85000.00"));

        when(employeeRepository.existsByEmailIgnoreCase("sarah.new@hrgenius.com")).thenReturn(false);
        when(departmentRepository.findByNameIgnoreCase("Software Engineering")).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals("Sarah Jenkins", response.getName());
        assertEquals("Software Engineering", response.getDepartment());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_DepartmentChangeSynchronized() {
        Department hrDept = new Department("DEP-101", "HR Resource", "HR-01", "HR", "Sarah Jenkins");
        hrDept.setId(1L);

        Department sweDept = new Department("DEP-102", "Software Engineering", "ENG-02", "Dev", "David Chen");
        sweDept.setId(2L);

        Employee shivam = new Employee();
        shivam.setId(10L);
        shivam.setEmpId("EMP-010");
        shivam.setName("Shivam Verma");
        shivam.setEmail("shivam@hrgenius.com");
        shivam.setDepartment(hrDept);
        shivam.setStatus("Active");

        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setEmpId("EMP-010");
        updateRequest.setName("Shivam Verma");
        updateRequest.setEmail("shivam@hrgenius.com");
        updateRequest.setPhone("+1 555-0999");
        updateRequest.setDepartment("Software Engineering");
        updateRequest.setDepartmentId(1L); // Simulating old/stale departmentId from previous state

        when(employeeRepository.findByIdWithDepartment(10L)).thenReturn(Optional.of(shivam));
        when(departmentRepository.findByNameIgnoreCase("Software Engineering")).thenReturn(Optional.of(sweDept));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse updated = employeeService.updateEmployee(10L, updateRequest);

        assertNotNull(updated);
        assertEquals("Software Engineering", updated.getDepartment());
        assertEquals(2L, updated.getDepartmentId());
        assertEquals(sweDept, shivam.getDepartment());
        verify(employeeRepository).save(shivam);
    }
}
