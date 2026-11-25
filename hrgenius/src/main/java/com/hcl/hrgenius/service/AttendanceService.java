package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.attendance.AttendanceRequest;
import com.hcl.hrgenius.dto.attendance.AttendanceResponse;
import com.hcl.hrgenius.entity.Attendance;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendance(String search, String status, LocalDate date) {
        List<Attendance> list;
        if ((search == null || search.trim().isEmpty()) &&
            (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status)) &&
            date == null) {
            list = attendanceRepository.findAll();
        } else {
            list = attendanceRepository.searchAttendance(search, status, date);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id) {
        Attendance record = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
        return mapToResponse(record);
    }

    public AttendanceResponse createAttendance(AttendanceRequest request) {
        LocalDate attDate = request.getDate() != null ? request.getDate() : LocalDate.now();
        String searchTarget = request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                ? request.getEmployeeId().trim()
                : (request.getEmployee() != null ? request.getEmployee().trim() : null);

        if (searchTarget != null) {
            List<Attendance> existing = attendanceRepository.searchAttendance(searchTarget, null, attDate);
            if (!existing.isEmpty()) {
                Attendance record = existing.get(0);
                if (request.getCheckOut() != null && !"-".equals(request.getCheckOut()) && !request.getCheckOut().isBlank()) {
                    record.setCheckOut(request.getCheckOut());
                }
                if (request.getCheckIn() != null && !request.getCheckIn().isBlank() && "-".equals(record.getCheckIn())) {
                    record.setCheckIn(request.getCheckIn());
                }
                if (request.getStatus() != null) record.setStatus(request.getStatus());
                if (request.getRemarks() != null) record.setRemarks(request.getRemarks());
                Attendance updated = attendanceRepository.save(record);
                return mapToResponse(updated);
            }
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(request.getEmployee());
        attendance.setEmployeeId(request.getEmployeeId());
        attendance.setDate(attDate);
        attendance.setCheckIn(request.getCheckIn() != null ? request.getCheckIn() : "09:00 AM");
        attendance.setCheckOut(request.getCheckOut() != null ? request.getCheckOut() : "-");
        attendance.setStatus(request.getStatus() != null ? request.getStatus() : "Present");
        attendance.setRemarks(request.getRemarks());

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));

        if (request.getEmployee() != null) attendance.setEmployee(request.getEmployee());
        if (request.getEmployeeId() != null) attendance.setEmployeeId(request.getEmployeeId());
        if (request.getDate() != null) attendance.setDate(request.getDate());
        if (request.getCheckIn() != null) attendance.setCheckIn(request.getCheckIn());
        if (request.getCheckOut() != null) attendance.setCheckOut(request.getCheckOut());
        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getRemarks() != null) attendance.setRemarks(request.getRemarks());

        Attendance updated = attendanceRepository.save(attendance);
        return mapToResponse(updated);
    }

    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance record not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        AttendanceResponse resp = new AttendanceResponse();
        resp.setId(attendance.getId());
        resp.setEmployee(attendance.getEmployee());
        resp.setEmployeeId(attendance.getEmployeeId());
        resp.setDate(attendance.getDate());
        resp.setCheckIn(attendance.getCheckIn());
        resp.setCheckOut(attendance.getCheckOut());
        resp.setStatus(attendance.getStatus());
        resp.setRemarks(attendance.getRemarks());
        return resp;
    }
}
