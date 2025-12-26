package com.app.easypharma_backend.domain.order.service.interfaces;

import com.app.easypharma_backend.domain.order.dto.CreateOrderDTO;
import com.app.easypharma_backend.domain.order.dto.OrderDTO;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

public interface OrderServiceInterface {

    /**
     * Create a new order for the authenticated patient.
     * Validates stock and pricing.
     */
    OrderDTO createOrder(@NonNull UUID patientId, @NonNull CreateOrderDTO createOrderDTO);

    /**
     * Get details of a specific order.
     */
    OrderDTO getOrderById(@NonNull UUID orderId);

    /**
     * Get all orders for a specific patient.
     */
    List<OrderDTO> getPatientOrders(@NonNull UUID patientId);

    /**
     * Get all orders for a specific pharmacy.
     */
    List<OrderDTO> getPharmacyOrders(@NonNull UUID pharmacyId);

    /**
     * Update order status (e.g., CONFIRMED, DELIVERED).
     */
    OrderDTO updateOrderStatus(@NonNull UUID orderId, @NonNull OrderStatus status);

    /**
     * Verify stock availability for a list of items.
     */
    void validateStockAvailability(@NonNull UUID pharmacyId, @NonNull List<CreateOrderDTO.CreateOrderItemDTO> items);

    /**
     * Get total revenue for a pharmacy (DELIVERED orders).
     */
    java.math.BigDecimal getPharmacyRevenue(@NonNull UUID pharmacyId);
}
