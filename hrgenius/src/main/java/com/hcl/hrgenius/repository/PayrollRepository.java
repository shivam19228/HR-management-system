package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    @Query("SELECT p FROM Payroll p WHERE " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(p.status) = LOWER(:status)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.employee) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.employeeId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.payMonth) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.id DESC")
    List<Payroll> searchPayroll(@Param("search") String search, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p")
    BigDecimal sumTotalPayroll();
}
