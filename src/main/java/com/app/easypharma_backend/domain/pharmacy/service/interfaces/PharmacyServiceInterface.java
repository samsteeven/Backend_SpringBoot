package com.app.easypharma_backend.domain.pharmacy.service.interfaces;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

public interface PharmacyServiceInterface {

    /**
     * Récupère toutes les pharmacies
     */
    List<PharmacyDTO> getAllPharmacies();

    /**
     * Récupère toutes les pharmacies pour l'admin (y compris Pending/Rejected)
     */
    List<PharmacyDTO> getAllPharmaciesForAdmin();

    /**
     * Récupère une pharmacie par ID
     */
    PharmacyDTO getPharmacyById(@NonNull UUID id);

    /**
     * Récupère une pharmacie par numéro de licence
     */
    PharmacyDTO getPharmacyByLicenseNumber(@NonNull String licenseNumber);

    /**
     * Crée une nouvelle pharmacie
     */
    PharmacyDTO createPharmacy(@NonNull PharmacyDTO pharmacyDTO);

    /**
     * Met à jour une pharmacie existante
     */
    PharmacyDTO updatePharmacy(@NonNull UUID id, @NonNull PharmacyDTO pharmacyDTO);

    /**
     * Change le statut d'une pharmacie (APPROVED/REJECTED/SUSPENDED)
     */
    PharmacyDTO changeStatus(@NonNull UUID id, @NonNull PharmacyStatus status);

    /**
     * Recherche pharmacies par ville
     */
    List<PharmacyDTO> findByCity(@NonNull String city);

    /**
     * Recherche pharmacies par statut
     */
    List<PharmacyDTO> findByStatus(@NonNull PharmacyStatus status);

    /**
     * Recherche pharmacies approuvées dans une ville
     */
    List<PharmacyDTO> findApprovedByCity(@NonNull String city);

    /**
     * Recherche pharmacies à proximité
     */
    List<PharmacyDTO> findNearbyPharmacies(@NonNull Double latitude, @NonNull Double longitude,
            @NonNull Double radiusKm);

    /**
     * Recherche par nom (LIKE)
     */
    List<PharmacyDTO> searchByName(@NonNull String name);

    /**
     * Supprime une pharmacie
     */
    void deletePharmacy(@NonNull UUID id);

    /**
     * Vérifie si le numéro de licence existe déjà
     */
    boolean existsByLicenseNumber(@NonNull String licenseNumber);
}
