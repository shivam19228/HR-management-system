package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmpId(String empId);

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByNameIgnoreCase(String name);

    boolean existsByEmpId(String empId);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department ORDER BY e.id DESC")
    List<Employee> findAllWithDepartment();

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department d WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.empId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.job) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " (d IS NOT NULL AND LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "((:deptId IS NULL AND (:department IS NULL OR :department = 'All' OR :department = '')) OR " +
           " (d IS NOT NULL AND (" +
           "   (:deptId IS NOT NULL AND d.id = :deptId) OR " +
           "   (:department IS NOT NULL AND (" +
           "     LOWER(d.name) = LOWER(:department) OR " +
           "     LOWER(d.departmentId) = LOWER(:department) OR " +
           "     LOWER(d.code) = LOWER(:department)" +
           "   ))" +
           " ))) AND " +
           "(:status IS NULL OR :status = 'All' OR :status = '' OR LOWER(e.status) = LOWER(:status)) " +
           "ORDER BY e.id DESC")
    List<Employee> searchEmployees(
            @Param("search") String search,
            @Param("department") String department,
            @Param("deptId") Long deptId,
            @Param("status") String status);

    @Query("SELECT COALESCE(AVG(e.salary), 0) FROM Employee e")
    BigDecimal findAverageSalary();
}
