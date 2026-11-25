package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentRecord, Long> {

    @Query("SELECT d FROM DocumentRecord d WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(d.status) = LOWER(:status)) AND " +
           "(:type IS NULL OR :type = 'All' OR :type = '' OR LOWER(d.documentType) = LOWER(:type)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.employee) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.documentName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.id DESC")
    List<DocumentRecord> searchDocuments(
            @Param("search") String search,
            @Param("status") String status,
            @Param("type") String type);
}
