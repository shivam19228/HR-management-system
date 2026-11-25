package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(a.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.employee) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:date IS NULL OR a.date = :date) " +
           "ORDER BY a.date DESC, a.id DESC")
    List<Attendance> searchAttendance(
            @Param("search") String search,
            @Param("status") String status,
            @Param("date") LocalDate date);

    long countByStatus(String status);
}
