package com.app.easypharma_backend.domain.order.service.interfaces;

import com.app.easypharma_backend.domain.order.dto.CreateOrderDTO;
import com.app.easypharma_backend.domain.order.dto.OrderDTO;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderServiceInterface {

    /**
     * Create a new order for the authenticated patient.
     * Validates stock and pricing.
     */
    OrderDTO createOrder(UUID patientId, CreateOrderDTO createOrderDTO);

    /**
     * Get details of a specific order.
     */
    OrderDTO getOrderById(UUID orderId);

    /**
     * Get all orders for a specific patient.
     */
    List<OrderDTO> getPatientOrders(UUID patientId);

    /**
     * Get all orders for a specific pharmacy.
     */
    List<OrderDTO> getPharmacyOrders(UUID pharmacyId);

    /**
     * Update order status (e.g., CONFIRMED, DELIVERED).
     */
    OrderDTO updateOrderStatus(UUID orderId, OrderStatus status);

    /**
     * Verify stock availability for a list of items.
     */
    void validateStockAvailability(UUID pharmacyId, List<CreateOrderDTO.CreateOrderItemDTO> items);
}
