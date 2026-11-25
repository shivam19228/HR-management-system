package com.hcl.hrgenius.repository;

import com.hcl.hrgenius.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByNameIgnoreCase(String name);

    Optional<Department> findByDepartmentId(String departmentId);

    Optional<Department> findByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByDepartmentId(String departmentId);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees ORDER BY d.id ASC")
    List<Department> findAllWithEmployees();
}
