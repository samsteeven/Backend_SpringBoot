package com.app.easypharma_backend.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {
    private UUID pharmacyId;
    private List<CreateOrderItemDTO> items;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPhone;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String notes;
    private java.math.BigDecimal deliveryFee;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderItemDTO {
        private UUID medicationId;
        private Integer quantity;
    }
}
