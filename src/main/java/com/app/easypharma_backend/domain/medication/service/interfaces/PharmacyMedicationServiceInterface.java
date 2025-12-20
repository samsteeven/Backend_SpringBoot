package com.app.easypharma_backend.domain.medication.service.interfaces;

import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

public interface PharmacyMedicationServiceInterface {

    /**
     * Add a medication to a pharmacy with initial stock and price.
     */
    PharmacyMedicationDTO addMedicationToPharmacy(@NonNull UUID pharmacyId, @NonNull UUID medicationId,
            BigDecimal price, Integer stock);

    /**
     * Update the stock quantity of a medication in a pharmacy.
     */
    PharmacyMedicationDTO updateStock(@NonNull UUID pharmacyId, @NonNull UUID medicationId, Integer quantity);

    /**
     * Update the price of a medication in a pharmacy.
     */
    PharmacyMedicationDTO updatePrice(@NonNull UUID pharmacyId, @NonNull UUID medicationId, BigDecimal price);

    /**
     * Get all medications available in a specific pharmacy.
     */
    List<PharmacyMedicationDTO> getPharmacyInventory(@NonNull UUID pharmacyId);

    /**
     * Get details of a specific medication in a pharmacy.
     */
    PharmacyMedicationDTO getPharmacyMedication(@NonNull UUID pharmacyId, @NonNull UUID medicationId);

    /**
     * Remove a medication from pharmacy inventory.
     */
    void removeMedicationFromPharmacy(@NonNull UUID pharmacyId, @NonNull UUID medicationId);
}
