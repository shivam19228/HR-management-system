package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<PerformanceReview, Long> {

    @Query("SELECT p FROM PerformanceReview p WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.employee) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.employeeId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.goals) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.achievements) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating) " +
           "ORDER BY p.reviewDate DESC, p.id DESC")
    List<PerformanceReview> searchReviews(@Param("search") String search, @Param("minRating") Integer minRating);
}
