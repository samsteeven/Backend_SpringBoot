package com.app.easypharma_backend.domain.pharmacy.repository;

import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, UUID> {

    // Recherche par numéro de licence
    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);

    // Recherche par utilisateur
    Optional<Pharmacy> findByUserId(UUID userId);

    // Vérifier si numéro de licence existe
    boolean existsByLicenseNumber(String licenseNumber);

    // Recherche par ville
    List<Pharmacy> findByCity(String city);

    // Recherche par statut
    List<Pharmacy> findByStatus(PharmacyStatus status);

    // Compter par statut
    long countByStatus(PharmacyStatus status);

    // Recherche pharmacies approuvées
    List<Pharmacy> findByStatusOrderByNameAsc(PharmacyStatus status);

    // Recherche pharmacies approuvées dans une ville
    List<Pharmacy> findByStatusAndCity(PharmacyStatus status, String city);

    // Recherche par nom (case insensitive, LIKE)
    @Query("SELECT p FROM Pharmacy p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Pharmacy> searchByName(@Param("name") String name);

    // Recherche par nom (pharmacies approuvées uniquement)
    @Query("SELECT p FROM Pharmacy p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.status = 'APPROVED'")
    List<Pharmacy> searchByNameAndApproved(@Param("name") String name);

    // Recherche pharmacies à proximité (rayon en km)
    // Utilise la formule de Haversine pour calculer la distance
    @Query(value = """
            SELECT * FROM pharmacies p
            WHERE p.status = 'APPROVED'
            AND ST_DistanceSphere(
                ST_MakePoint(CAST(p.longitude AS DOUBLE PRECISION), CAST(p.latitude AS DOUBLE PRECISION)),
                ST_MakePoint(:longitude, :latitude)
            ) <= :radiusKm * 1000
            ORDER BY ST_DistanceSphere(
                ST_MakePoint(CAST(p.longitude AS DOUBLE PRECISION), CAST(p.latitude AS DOUBLE PRECISION)),
                ST_MakePoint(:longitude, :latitude)
            )
            """, nativeQuery = true)
    List<Pharmacy> findNearbyPharmacies(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm);
}