package com.app.easypharma_backend.domain.order.dto;

import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private UUID id;
    private String orderNumber;
    private UUID patientId;
    private String patientName;
    private UUID pharmacyId;
    private String pharmacyName;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private OrderStatus status;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPhone;
    private LocalDateTime created;
    private List<OrderItemDTO> items;
}
