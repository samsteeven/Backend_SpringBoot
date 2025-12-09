package com.app.easypharma_backend.domain.medication.repository;

import com.app.easypharma_backend.domain.medication.entity.PharmacyMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PharmacyMedicationRepository extends JpaRepository<PharmacyMedication, UUID> {

    // Recherche par pharmacie
    List<PharmacyMedication> findByPharmacyId(UUID pharmacyId);

    // Recherche par médicament
    List<PharmacyMedication> findByMedicationId(UUID medicationId);

    // Recherche par pharmacie et médicament
    Optional<PharmacyMedication> findByPharmacyIdAndMedicationId(UUID pharmacyId, UUID medicationId);

    // Recherche médicaments disponibles d'une pharmacie
    List<PharmacyMedication> findByPharmacyIdAndIsAvailableTrue(UUID pharmacyId);

    // Recherche médicaments en stock faible (< seuil)
    @Query("SELECT pm FROM PharmacyMedication pm WHERE pm.pharmacy.id = :pharmacyId AND pm.stockQuantity < :threshold")
    List<PharmacyMedication> findLowStockMedications(@Param("pharmacyId") UUID pharmacyId, @Param("threshold") Integer threshold);

    // Comparaison de prix pour un médicament
    @Query("""
        SELECT pm FROM PharmacyMedication pm
        WHERE pm.medication.id = :medicationId
        AND pm.isAvailable = true
        AND pm.pharmacy.status = 'APPROVED'
        ORDER BY pm.price ASC
        """)
    List<PharmacyMedication> findAvailablePricesForMedication(@Param("medicationId") UUID medicationId);

    // Recherche par plage de prix
    @Query("SELECT pm FROM PharmacyMedication pm WHERE pm.pharmacy.id = :pharmacyId AND pm.price BETWEEN :minPrice AND :maxPrice")
    List<PharmacyMedication> findByPharmacyAndPriceRange(
            @Param("pharmacyId") UUID pharmacyId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}