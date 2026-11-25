package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.leave.LeaveRequestDto;
import com.hcl.hrgenius.dto.leave.LeaveResponseDto;
import com.hcl.hrgenius.entity.LeaveRequest;
import com.hcl.hrgenius.repository.LeaveRequestRepository;
import com.hcl.hrgenius.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest sampleLeave;

    @BeforeEach
    void setUp() {
        sampleLeave = new LeaveRequest("Alex Rivera", "EMP-006", "Sick Leave",
                LocalDate.now(), LocalDate.now().plusDays(2), 2, LocalDate.now(), "Medical rest", "Pending");
        sampleLeave.setId(1L);
    }

    @Test
    void testGetLeaves_ReturnsList() {
        when(leaveRepository.findAll()).thenReturn(List.of(sampleLeave));

        List<LeaveResponseDto> list = leaveService.getLeaves(null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Alex Rivera", list.get(0).getEmployee());
        assertEquals("Pending", list.get(0).getStatus());
    }

    @Test
    void testCreateLeave_Success() {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setEmployee("Alex Rivera");
        request.setEmployeeId("EMP-006");
        request.setLeaveType("Sick Leave");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setTotalDays(2);
        request.setStatus("Pending");

        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(sampleLeave);

        LeaveResponseDto response = leaveService.createLeave(request);

        assertNotNull(response);
        assertEquals("Alex Rivera", response.getEmployee());
        assertEquals("Sick Leave", response.getLeaveType());
    }

    @Test
    void testGetLeaveById_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(sampleLeave));

        LeaveResponseDto response = leaveService.getLeaveById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Alex Rivera", response.getEmployee());
    }
}
