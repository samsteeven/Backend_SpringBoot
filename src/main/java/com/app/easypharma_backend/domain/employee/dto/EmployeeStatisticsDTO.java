package com.app.easypharma_backend.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for employee statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStatisticsDTO {
    private UUID employeeId;
    private String firstName;
    private String lastName;
    private int ordersProcessedToday;
    private int ordersProcessedWeek;
    private int ordersProcessedMonth;
    private int deliveriesAssignedToday;
    private int deliveriesAssignedWeek;
    private int deliveriesAssignedMonth;
    private double averageProcessingTime;
}
