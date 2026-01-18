package com.app.easypharma_backend.domain.order.repository;

import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

        // Recherche par numéro de commande
        Optional<Order> findByOrderNumber(String orderNumber);

        // Recherche par patient (avec JOIN FETCH pour éviter
        // LazyInitializationException)
        @Query("SELECT DISTINCT o FROM Order o " +
                        "LEFT JOIN FETCH o.patient " +
                        "LEFT JOIN FETCH o.pharmacy " +
                        "LEFT JOIN FETCH o.items " +
                        "WHERE o.patient.id = :patientId " +
                        "ORDER BY o.createdAt DESC")
        List<Order> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") UUID patientId);

        // Recherche par pharmacie (avec JOIN FETCH pour éviter
        // LazyInitializationException)
        @Query("SELECT DISTINCT o FROM Order o " +
                        "LEFT JOIN FETCH o.patient " +
                        "LEFT JOIN FETCH o.pharmacy " +
                        "LEFT JOIN FETCH o.items " +
                        "WHERE o.pharmacy.id = :pharmacyId " +
                        "ORDER BY o.createdAt DESC")
        List<Order> findByPharmacyIdOrderByCreatedAtDesc(@Param("pharmacyId") UUID pharmacyId);

        // Recherche par patient avec pagination
        Page<Order> findByPatientId(UUID patientId, Pageable pageable);

        // Recherche par pharmacie avec pagination
        Page<Order> findByPharmacyId(UUID pharmacyId, Pageable pageable);

        // Recherche par statut
        List<Order> findByStatus(OrderStatus status);

        // Recherche par patient et statut
        List<Order> findByPatientIdAndStatus(UUID patientId, OrderStatus status);

        // Recherche par pharmacie et statut
        List<Order> findByPharmacyIdAndStatus(UUID pharmacyId, OrderStatus status);

        // Commandes dans une période
        @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
        List<Order> findOrdersBetweenDates(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Statistiques pharmacie (nombre commandes par statut)
        @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.pharmacy.id = :pharmacyId GROUP BY o.status")
        List<Object[]> getOrderStatsByPharmacy(@Param("pharmacyId") UUID pharmacyId);

        // Chiffre d'affaires pharmacie
        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.pharmacy.id = :pharmacyId AND o.status = com.app.easypharma_backend.domain.order.entity.OrderStatus.DELIVERED")
        BigDecimal calculateTotalRevenueByPharmacy(@Param("pharmacyId") UUID pharmacyId);

        // --- GLOBAL STATS FOR ADMIN ---

        @Query("SELECT COUNT(o) FROM Order o")
        long countTotalOrders();

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = com.app.easypharma_backend.domain.order.entity.OrderStatus.DELIVERED")
        BigDecimal calculateGlobalRevenue();

        @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.app.easypharma_backend.domain.order.entity.OrderStatus.PENDING")
        long countPendingOrders();

        // Top Selling Medications (Global)
        @Query("SELECT oi.medication.name, SUM(oi.quantity) as totalSold FROM OrderItem oi JOIN oi.order o WHERE o.status = com.app.easypharma_backend.domain.order.entity.OrderStatus.DELIVERED GROUP BY oi.medication.name ORDER BY totalSold DESC")
        List<Object[]> findTopSellingMedications(Pageable pageable);
}