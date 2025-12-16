package com.app.easypharma_backend.domain.pharmacy.service.interfaces;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;

import java.util.List;
import java.util.UUID;

public interface PharmacyServiceInterface {

    /**
     * Récupère toutes les pharmacies
     */
    List<PharmacyDTO> getAllPharmacies();

    /**
     * Récupère une pharmacie par ID
     */
    PharmacyDTO getPharmacyById(UUID id);

    /**
     * Récupère une pharmacie par numéro de licence
     */
    PharmacyDTO getPharmacyByLicenseNumber(String licenseNumber);

    /**
     * Crée une nouvelle pharmacie
     */
    PharmacyDTO createPharmacy(PharmacyDTO pharmacyDTO);

    /**
     * Met à jour une pharmacie existante
     */
    PharmacyDTO updatePharmacy(UUID id, PharmacyDTO pharmacyDTO);

    /**
     * Change le statut d'une pharmacie (APPROVED/REJECTED/SUSPENDED)
     */
    PharmacyDTO changeStatus(UUID id, PharmacyStatus status);

    /**
     * Recherche pharmacies par ville
     */
    List<PharmacyDTO> findByCity(String city);

    /**
     * Recherche pharmacies par statut
     */
    List<PharmacyDTO> findByStatus(PharmacyStatus status);

    /**
     * Recherche pharmacies approuvées dans une ville
     */
    List<PharmacyDTO> findApprovedByCity(String city);

    /**
     * Recherche pharmacies à proximité
     */
    List<PharmacyDTO> findNearbyPharmacies(Double latitude, Double longitude, Double radiusKm);

    /**
     * Recherche par nom (LIKE)
     */
    List<PharmacyDTO> searchByName(String name);

    /**
     * Supprime une pharmacie
     */
    void deletePharmacy(UUID id);

    /**
     * Vérifie si le numéro de licence existe déjà
     */
    boolean existsByLicenseNumber(String licenseNumber);
}
