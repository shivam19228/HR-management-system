package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.user.ProfileResponse;
import com.hcl.hrgenius.entity.Department;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.entity.User;
import com.hcl.hrgenius.exception.UnauthorizedException;
import com.hcl.hrgenius.repository.EmployeeRepository;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProfileService profileService;

    private User employeeUser;
    private Employee employee;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department("DEP-102", "Software Engineering", "ENG-02", "Dev", "David Chen");
        department.setId(2L);

        employeeUser = new User("jessica_dev", "pass", "jessica@hrgenius.com", "Jessica Taylor", "Employee", "Active");
        employeeUser.setId(4L);

        employee = new Employee();
        employee.setId(3L);
        employee.setEmpId("EMP-003");
        employee.setName("Jessica Taylor");
        employee.setEmail("jessica@hrgenius.com");
        employee.setPhone("+1 (555) 345-6789");
        employee.setDepartment(department);
        employee.setJob("Software Developer");
        employee.setDesignation("Software Developer");
        employee.setJoiningDate(LocalDate.of(2023, 1, 15));
        employee.setSalary(new BigDecimal("82000.00"));
        employee.setStatus("Active");
    }

    @Test
    void testGetProfile_Unauthenticated_ThrowsUnauthorized() {
        when(securityHelper.getAuthenticatedUser(request)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> profileService.getCurrentUserProfile(request));
    }

    @Test
    void testGetProfile_EmployeeWithDepartment() {
        when(securityHelper.getAuthenticatedUser(request)).thenReturn(employeeUser);
        when(securityHelper.getAssociatedEmployee(employeeUser)).thenReturn(Optional.of(employee));

        ProfileResponse profile = profileService.getCurrentUserProfile(request);

        assertNotNull(profile);
        assertEquals("jessica_dev", profile.getUsername());
        assertEquals("Jessica Taylor", profile.getName());
        assertEquals("jessica@hrgenius.com", profile.getEmail());
        assertEquals("EMP-003", profile.getEmpId());
        assertEquals("Software Engineering", profile.getDepartment());
        assertEquals(2L, profile.getDepartmentId());
        assertEquals("Software Developer", profile.getJob());
        assertEquals(new BigDecimal("82000.00"), profile.getSalary());
    }

    @Test
    void testGetProfile_AdminRoleWithoutLinkedEmployee() {
        User adminUser = new User("admin", "pass", "admin@hrgenius.com", "Sarah Jenkins", "Admin", "Active");
        adminUser.setId(1L);

        when(securityHelper.getAuthenticatedUser(request)).thenReturn(adminUser);
        when(securityHelper.getAssociatedEmployee(adminUser)).thenReturn(Optional.empty());

        ProfileResponse profile = profileService.getCurrentUserProfile(request);

        assertNotNull(profile);
        assertEquals("admin", profile.getUsername());
        assertEquals("Admin", profile.getRole());
        assertEquals("Sarah Jenkins", profile.getName());
        assertEquals("System Administration", profile.getDepartment());
        assertTrue(profile.getEmpId().startsWith("ADM-"));
    }
}
