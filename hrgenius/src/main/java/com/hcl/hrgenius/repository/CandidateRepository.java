package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByCandidateId(String candidateId);

    @Query("SELECT c FROM Candidate c WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(c.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.candidateId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.appliedJob) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.id DESC")
    List<Candidate> searchCandidates(@Param("search") String search, @Param("status") String status);
}
