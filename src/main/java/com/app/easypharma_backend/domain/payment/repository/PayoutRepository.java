package com.app.easypharma_backend.domain.payment.repository;

import com.app.easypharma_backend.domain.payment.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    List<Payout> findByPharmacyIdOrderByProcessedAtDesc(UUID pharmacyId);
}
