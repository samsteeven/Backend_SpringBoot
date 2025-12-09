package com.app.easypharma_backend.domain.order.repository;

import com.app.easypharma_backend.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // Recherche par commande
    List<OrderItem> findByOrderId(UUID orderId);

    // Recherche par médicament
    List<OrderItem> findByMedicationId(UUID medicationId);

    // Top médicaments vendus
    @Query("""
        SELECT oi.medication.id, oi.medication.name, SUM(oi.quantity) as totalQuantity
        FROM OrderItem oi
        WHERE oi.order.status = 'DELIVERED'
        GROUP BY oi.medication.id, oi.medication.name
        ORDER BY totalQuantity DESC
        """)
    List<Object[]> findTopSellingMedications();
}