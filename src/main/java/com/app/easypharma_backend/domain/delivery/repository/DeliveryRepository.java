package com.app.easypharma_backend.domain.delivery.repository;

import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import com.app.easypharma_backend.domain.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    // Recherche par commande
    Optional<Delivery> findByOrderId(UUID orderId);

    // Recherche par livreur
    List<Delivery> findByDeliveryPersonIdOrderByCreatedAtDesc(UUID deliveryPersonId);

    // Recherche par statut
    List<Delivery> findByStatus(DeliveryStatus status);

    // Recherche par livreur et statut
    List<Delivery> findByDeliveryPersonIdAndStatus(UUID deliveryPersonId, DeliveryStatus status);

    // Livraisons en cours d'un livreur
    @Query("""
        SELECT d FROM Delivery d
        WHERE d.deliveryPerson.id = :deliveryPersonId
        AND d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT')
        ORDER BY d.assignedAt ASC
        """)
    List<Delivery> findOngoingDeliveriesByPerson(@Param("deliveryPersonId") UUID deliveryPersonId);

    // Livraisons dans une période
    @Query("SELECT d FROM Delivery d WHERE d.createdAt BETWEEN :startDate AND :endDate ORDER BY d.createdAt DESC")
    List<Delivery> findDeliveriesBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Livraisons complétées par livreur
    @Query("""
        SELECT COUNT(d) FROM Delivery d
        WHERE d.deliveryPerson.id = :deliveryPersonId
        AND d.status = 'DELIVERED'
        """)
    Long countCompletedDeliveriesByPerson(@Param("deliveryPersonId") UUID deliveryPersonId);

    // Livraisons échouées par livreur
    @Query("""
        SELECT COUNT(d) FROM Delivery d
        WHERE d.deliveryPerson.id = :deliveryPersonId
        AND d.status = 'FAILED'
        """)
    Long countFailedDeliveriesByPerson(@Param("deliveryPersonId") UUID deliveryPersonId);

    // Temps moyen de livraison (en minutes)
    @Query(
            value = "SELECT AVG(EXTRACT(EPOCH FROM (d.delivered_at - d.picked_up_at)) / 60) " +
                    "FROM delivery d " +
                    "WHERE d.status = 'DELIVERED' " +
                    "AND d.picked_up_at IS NOT NULL " +
                    "AND d.delivered_at IS NOT NULL",
            nativeQuery = true
    )
    Double calculateAverageDeliveryTimeInMinutes();


    // Vérifier si commande a déjà une livraison
    boolean existsByOrderId(UUID orderId);
}