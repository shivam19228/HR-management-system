package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByJobId(String jobId);

    boolean existsByJobId(String jobId);

    @Query("SELECT j FROM Job j WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(j.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(j.jobId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(j.department) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY j.id DESC")
    List<Job> searchJobs(@Param("search") String search, @Param("status") String status);

    long countByStatus(String status);
}
