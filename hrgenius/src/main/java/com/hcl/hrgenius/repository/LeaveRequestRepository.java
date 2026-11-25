package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(l.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(l.employee) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.employeeId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.leaveType) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY l.id DESC")
    List<LeaveRequest> searchLeaves(@Param("search") String search, @Param("status") String status);

    long countByStatus(String status);
}
