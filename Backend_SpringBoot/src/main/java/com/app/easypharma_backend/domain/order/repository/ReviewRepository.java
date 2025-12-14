package com.app.easypharma_backend.domain.order.repository;

import com.app.easypharma_backend.domain.order.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Recherche par commande
    Optional<Review> findByOrderId(UUID orderId);

    // Recherche par pharmacie
    List<Review> findByPharmacyIdOrderByCreatedAtDesc(UUID pharmacyId);

    // Recherche par patient
    List<Review> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    // Recherche avis approuvés d'une pharmacie
    List<Review> findByPharmacyIdAndStatusOrderByCreatedAtDesc(UUID pharmacyId, String status);

    // Note moyenne d'une pharmacie
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.pharmacy.id = :pharmacyId AND r.status = 'APPROVED'")
    Double getAverageRatingByPharmacy(@Param("pharmacyId") UUID pharmacyId);

    // Nombre d'avis par pharmacie
    @Query("SELECT COUNT(r) FROM Review r WHERE r.pharmacy.id = :pharmacyId AND r.status = 'APPROVED'")
    Long countApprovedReviewsByPharmacy(@Param("pharmacyId") UUID pharmacyId);
}