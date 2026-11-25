package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("SELECT i FROM Interview i WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(i.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(i.candidate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.job) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.interviewer) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY i.id DESC")
    List<Interview> searchInterviews(@Param("search") String search, @Param("status") String status);
}
