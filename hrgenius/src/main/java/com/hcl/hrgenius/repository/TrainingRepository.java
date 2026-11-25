package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingProgram, Long> {

    @Query("SELECT t FROM TrainingProgram t WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(t.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(t.employee) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.trainingName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.trainer) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.id DESC")
    List<TrainingProgram> searchTrainings(@Param("search") String search, @Param("status") String status);
}
