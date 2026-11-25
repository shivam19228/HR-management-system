package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.leave.LeaveRequestDto;
import com.hcl.hrgenius.dto.leave.LeaveResponseDto;
import com.hcl.hrgenius.entity.LeaveRequest;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;

    public LeaveService(LeaveRequestRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getLeaves(String search, String status) {
        List<LeaveRequest> list;
        if ((search == null || search.trim().isEmpty()) && (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))) {
            list = leaveRepository.findAll();
        } else {
            list = leaveRepository.searchLeaves(search, status);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveResponseDto getLeaveById(Long id) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        return mapToResponse(leave);
    }

    public LeaveResponseDto createLeave(LeaveRequestDto request) {
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(request.getEmployee());
        leave.setEmployeeId(request.getEmployeeId());
        leave.setLeaveType(request.getLeaveType() != null ? request.getLeaveType() : "Casual Leave");
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setTotalDays(request.getTotalDays() != null ? request.getTotalDays() : 1);
        leave.setAppliedDate(request.getAppliedDate() != null ? request.getAppliedDate() : LocalDate.now());
        leave.setReason(request.getReason());
        leave.setStatus(request.getStatus() != null ? request.getStatus() : "Pending");

        LeaveRequest saved = leaveRepository.save(leave);
        return mapToResponse(saved);
    }

    public LeaveResponseDto updateLeave(Long id, LeaveRequestDto request) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        if (request.getEmployee() != null) leave.setEmployee(request.getEmployee());
        if (request.getEmployeeId() != null) leave.setEmployeeId(request.getEmployeeId());
        if (request.getLeaveType() != null) leave.setLeaveType(request.getLeaveType());
        if (request.getStartDate() != null) leave.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) leave.setEndDate(request.getEndDate());
        if (request.getTotalDays() != null) leave.setTotalDays(request.getTotalDays());
        if (request.getAppliedDate() != null) leave.setAppliedDate(request.getAppliedDate());
        if (request.getReason() != null) leave.setReason(request.getReason());
        if (request.getStatus() != null) leave.setStatus(request.getStatus());

        LeaveRequest updated = leaveRepository.save(leave);
        return mapToResponse(updated);
    }

    public LeaveResponseDto updateLeaveStatus(Long id, String status) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        leave.setStatus(status);
        LeaveRequest updated = leaveRepository.save(leave);
        return mapToResponse(updated);
    }

    public void deleteLeave(Long id) {
        if (!leaveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Leave request not found with id: " + id);
        }
        leaveRepository.deleteById(id);
    }

    private LeaveResponseDto mapToResponse(LeaveRequest leave) {
        LeaveResponseDto resp = new LeaveResponseDto();
        resp.setId(leave.getId());
        resp.setEmployee(leave.getEmployee());
        resp.setEmployeeId(leave.getEmployeeId());
        resp.setLeaveType(leave.getLeaveType());
        resp.setStartDate(leave.getStartDate());
        resp.setEndDate(leave.getEndDate());
        resp.setTotalDays(leave.getTotalDays());
        resp.setAppliedDate(leave.getAppliedDate());
        resp.setReason(leave.getReason());
        resp.setStatus(leave.getStatus());
        return resp;
    }
}
