package com.app.easypharma_backend.domain.medication.service.interfaces;

import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PharmacyMedicationServiceInterface {

    /**
     * Add a medication to a pharmacy with initial stock and price.
     */
    PharmacyMedicationDTO addMedicationToPharmacy(UUID pharmacyId, UUID medicationId, BigDecimal price, Integer stock);

    /**
     * Update the stock quantity of a medication in a pharmacy.
     */
    PharmacyMedicationDTO updateStock(UUID pharmacyId, UUID medicationId, Integer quantity);

    /**
     * Update the price of a medication in a pharmacy.
     */
    PharmacyMedicationDTO updatePrice(UUID pharmacyId, UUID medicationId, BigDecimal price);

    /**
     * Get all medications available in a specific pharmacy.
     */
    List<PharmacyMedicationDTO> getPharmacyInventory(UUID pharmacyId);

    /**
     * Get details of a specific medication in a pharmacy.
     */
    PharmacyMedicationDTO getPharmacyMedication(UUID pharmacyId, UUID medicationId);

    /**
     * Remove a medication from pharmacy inventory.
     */
    void removeMedicationFromPharmacy(UUID pharmacyId, UUID medicationId);
}
