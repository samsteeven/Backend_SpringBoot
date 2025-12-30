package com.app.easypharma_backend.domain.order.repository;

import com.app.easypharma_backend.domain.order.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByPharmacyIdAndStatusOrderByCreatedAtDesc(UUID pharmacyId, String status);

    List<Review> findByCourierIdAndStatusOrderByCreatedAtDesc(UUID courierId, String status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.pharmacy.id = :pharmacyId AND r.status = 'APPROVED'")
    Double calculateAverageRating(@Param("pharmacyId") UUID pharmacyId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.pharmacy.id = :pharmacyId AND r.status = 'APPROVED'")
    Integer countApprovedReviews(@Param("pharmacyId") UUID pharmacyId);
}