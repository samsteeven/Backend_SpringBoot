package com.app.easypharma_backend.domain.payment.repository;

import com.app.easypharma_backend.domain.payment.entity.Payment;
import com.app.easypharma_backend.domain.payment.entity.PaymentMethod;
import com.app.easypharma_backend.domain.payment.entity.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Recherche par commande
    Optional<Payment> findByOrderId(UUID orderId);

    // Recherche par transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Recherche par statut
    List<Payment> findByStatus(PaymentStatus status);

    // Recherche par méthode de paiement
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    // Recherche paiements en attente
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    // Paiements dans une période
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Paiements réussis d'une pharmacie
    @Query("""
        SELECT p FROM Payment p
        WHERE p.order.pharmacy.id = :pharmacyId
        AND p.status = 'SUCCESS'
        ORDER BY p.paidAt DESC
        """)
    List<Payment> findSuccessfulPaymentsByPharmacy(@Param("pharmacyId") UUID pharmacyId);

    // Montant total encaissé par pharmacie
    @Query("""
        SELECT SUM(p.amount) FROM Payment p
        WHERE p.order.pharmacy.id = :pharmacyId
        AND p.status = 'SUCCESS'
        """)
    BigDecimal calculateTotalRevenue(@Param("pharmacyId") UUID pharmacyId);

    // Statistiques paiements par méthode
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();

    // Vérifier si commande a déjà un paiement
    boolean existsByOrderId(UUID orderId);
}