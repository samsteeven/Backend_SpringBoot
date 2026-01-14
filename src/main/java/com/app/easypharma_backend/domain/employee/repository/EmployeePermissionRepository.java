package com.app.easypharma_backend.domain.employee.repository;

import com.app.easypharma_backend.domain.employee.entity.EmployeePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing employee permissions
 */
@Repository
public interface EmployeePermissionRepository extends JpaRepository<EmployeePermission, UUID> {

    /**
     * Find permissions by employee ID
     */
    Optional<EmployeePermission> findByEmployeeId(UUID employeeId);

    /**
     * Check if permissions exist for an employee
     */
    boolean existsByEmployeeId(UUID employeeId);

    /**
     * Delete permissions by employee ID
     */
    void deleteByEmployeeId(UUID employeeId);
}
