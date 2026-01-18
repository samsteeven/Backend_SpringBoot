package com.app.easypharma_backend.domain.medication.repository;

import com.app.easypharma_backend.domain.medication.entity.PharmacyMedication;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

        @Lock(LockModeType.PESSIMISTIC_WRITE) // Empêche tout autre thread de lire/écrire pendant la transaction
        @Query("SELECT pm FROM PharmacyMedication pm WHERE pm.pharmacy.id = :pId AND pm.medication.id = :mId")
        Optional<PharmacyMedication> findByPharmacyIdAndMedicationIdForUpdate(@Param("pId") UUID pId,
                        @Param("mId") UUID mId);

        // Recherche médicaments disponibles d'une pharmacie
        List<PharmacyMedication> findByPharmacyIdAndIsAvailableTrue(UUID pharmacyId);

        // Recherche médicaments en stock faible (< seuil)
        @Query("SELECT pm FROM PharmacyMedication pm WHERE pm.pharmacy.id = :pharmacyId AND pm.stockQuantity < :threshold")
        List<PharmacyMedication> findLowStockMedications(@Param("pharmacyId") UUID pharmacyId,
                        @Param("threshold") Integer threshold);

        @Query("SELECT COUNT(pm) FROM PharmacyMedication pm WHERE pm.pharmacy.id = :pharmacyId AND pm.stockQuantity < :threshold")
        long countLowStock(@Param("pharmacyId") UUID pharmacyId, @Param("threshold") Integer threshold);

        @Query("SELECT COUNT(pm) FROM PharmacyMedication pm WHERE pm.pharmacy.id = :pharmacyId AND pm.expiryDate < :date AND pm.expiryDate > CURRENT_DATE")
        long countExpiringSoon(@Param("pharmacyId") UUID pharmacyId, @Param("date") java.time.LocalDate date);

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
                        @Param("maxPrice") BigDecimal maxPrice);

        // Recherche globale pour les patients
        @Query(value = """
                        SELECT pm.* FROM pharmacy_medications pm
                        JOIN medications m ON pm.medication_id = m.id
                        JOIN pharmacies p ON pm.pharmacy_id = p.id
                        WHERE pm.is_available = true
                        AND p.status = 'APPROVED'
                        AND (
                            m.name ILIKE '%' || CAST(:query AS text) || '%'
                            OR m.generic_name ILIKE '%' || CAST(:query AS text) || '%'
                            OR m.symptoms ILIKE '%' || CAST(:query AS text) || '%'
                        )
                        AND (CAST(:therapeuticClass AS text) IS NULL OR m.therapeutic_class = CAST(:therapeuticClass AS text))
                        """, nativeQuery = true)
        List<PharmacyMedication> searchGlobal(
                        @Param("query") String query,
                        @Param("therapeuticClass") String therapeuticClass);

        // Recherche avec tri PostGIS natif (ST_DistanceSphere)
        @Query(value = """
                        SELECT pm.* FROM pharmacy_medications pm
                        JOIN medications m ON pm.medication_id = m.id
                        JOIN pharmacies p ON pm.pharmacy_id = p.id
                        WHERE pm.is_available = true
                        AND p.status = 'APPROVED'
                        AND (
                            m.name ILIKE '%' || CAST(:query AS text) || '%'
                            OR m.generic_name ILIKE '%' || CAST(:query AS text) || '%'
                            OR m.symptoms ILIKE '%' || CAST(:query AS text) || '%'
                        )
                        AND (CAST(:therapeuticClass AS text) IS NULL OR m.therapeutic_class = CAST(:therapeuticClass AS text))
                        ORDER BY ST_DistanceSphere(
                            ST_MakePoint(CAST(p.longitude AS DOUBLE PRECISION), CAST(p.latitude AS DOUBLE PRECISION)),
                            ST_MakePoint(:userLon, :userLat)
                        ) ASC
                        """, nativeQuery = true)
        List<PharmacyMedication> searchGlobalPostGIS(
                        @Param("query") String query,
                        @Param("therapeuticClass") String therapeuticClass,
                        @Param("userLat") Double userLat,
                        @Param("userLon") Double userLon);
}