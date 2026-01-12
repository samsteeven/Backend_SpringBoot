package com.app.easypharma_backend.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for employee permissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePermissionDTO {
    private UUID id;
    private UUID employeeId;
    private Boolean canPrepareOrders;
    private Boolean canAssignDeliveries;
    private Boolean canViewStatistics;
    private Boolean canManageInventory;
    private Boolean canViewCustomerInfo;
    private Boolean canProcessPayments;
}
