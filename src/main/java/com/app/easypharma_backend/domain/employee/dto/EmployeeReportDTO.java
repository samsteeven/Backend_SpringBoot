package com.app.easypharma_backend.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for employee statistics/report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeReportDTO {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private int ordersProcessedToday;
    private int ordersProcessedWeek;
    private int ordersProcessedMonth;
    private int deliveriesAssignedToday;
    private int deliveriesAssignedWeek;
    private int deliveriesAssignedMonth;
    private double averageProcessingTime; // in minutes
}
