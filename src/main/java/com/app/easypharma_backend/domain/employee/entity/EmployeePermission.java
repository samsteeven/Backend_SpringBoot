package com.app.easypharma_backend.domain.employee.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing granular permissions for pharmacy employees.
 * Allows pharmacy admins to define specific capabilities for each employee.
 */
@Entity
@Table(name = "employee_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Reference to the employee (User entity with PHARMACY_EMPLOYEE role)
     */
    @Column(nullable = false, unique = true)
    private UUID employeeId;

    /**
     * Permission to prepare orders (change status from CONFIRMED to PREPARING to
     * READY)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canPrepareOrders = true;

    /**
     * Permission to assign deliveries to delivery personnel
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canAssignDeliveries = true;

    /**
     * Permission to view pharmacy statistics and analytics
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canViewStatistics = false;

    /**
     * Permission to manage inventory (add, update, delete medications)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canManageInventory = false;

    /**
     * Permission to view customer information
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canViewCustomerInfo = true;

    /**
     * Permission to process payments
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canProcessPayments = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ID of the admin who last modified these permissions
     */
    private UUID lastModifiedBy;
}
