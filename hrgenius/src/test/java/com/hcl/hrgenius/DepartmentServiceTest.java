package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.department.DepartmentRequest;
import com.hcl.hrgenius.dto.department.DepartmentResponse;
import com.hcl.hrgenius.entity.Department;
import com.hcl.hrgenius.repository.DepartmentRepository;
import com.hcl.hrgenius.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department sampleDepartment;

    @BeforeEach
    void setUp() {
        sampleDepartment = new Department("DEP-101", "Engineering", "ENG", "Tech division", "David Chen");
        sampleDepartment.setId(1L);
    }

    @Test
    void testGetAllDepartments() {
        when(departmentRepository.findAllWithEmployees()).thenReturn(List.of(sampleDepartment));

        List<DepartmentResponse> list = departmentService.getAllDepartments();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Engineering", list.get(0).getName());
        verify(departmentRepository, times(1)).findAllWithEmployees();
    }

    @Test
    void testCreateDepartment_Success() {
        DepartmentRequest request = new DepartmentRequest("DEP-101", "Engineering", "ENG", "Tech division", "David Chen");
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.existsByCodeIgnoreCase("ENG")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDepartment);

        DepartmentResponse response = departmentService.createDepartment(request);

        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertEquals("ENG", response.getCode());
    }

    @Test
    void testCreateDepartment_DuplicateNameThrowsException() {
        DepartmentRequest request = new DepartmentRequest("DEP-101", "Engineering", "ENG", "Tech division", "David Chen");
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> departmentService.createDepartment(request));
        verify(departmentRepository, never()).save(any(Department.class));
    }
}
