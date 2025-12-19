package com.app.easypharma_backend.domain.medication.service.implementation;

import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.PharmacyMedication;
import com.app.easypharma_backend.domain.medication.mapper.PharmacyMedicationMapper;
import com.app.easypharma_backend.domain.medication.repository.MedicationRepository;
import com.app.easypharma_backend.domain.medication.repository.PharmacyMedicationRepository;
import com.app.easypharma_backend.domain.medication.service.interfaces.PharmacyMedicationServiceInterface;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyMedicationServiceImplementation implements PharmacyMedicationServiceInterface {

    private final PharmacyMedicationRepository pharmacyMedicationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyMedicationMapper pharmacyMedicationMapper;

    @Override
    public PharmacyMedicationDTO addMedicationToPharmacy(UUID pharmacyId, UUID medicationId, BigDecimal price,
            Integer stock) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        // Check if already exists
        Optional<PharmacyMedication> existing = pharmacyMedicationRepository.findByPharmacyIdAndMedicationId(pharmacyId,
                medicationId);
        if (existing.isPresent()) {
            throw new RuntimeException("Medication already exists in this pharmacy. Update stock instead.");
        }

        PharmacyMedication pharmacyMedication = PharmacyMedication.builder()
                .pharmacy(pharmacy)
                .medication(medication)
                .price(price)
                .stockQuantity(stock)
                .isAvailable(stock > 0)
                .build();

        return pharmacyMedicationMapper.toDTO(pharmacyMedicationRepository.save(pharmacyMedication));
    }

    @Override
    public PharmacyMedicationDTO updateStock(UUID pharmacyId, UUID medicationId, Integer quantity) {
        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);

        item.setStockQuantity(quantity);
        // Availability update is handled by @PreUpdate in entity, but we can set it
        // explicitly too for clarity if needed
        // item.setIsAvailable(quantity > 0);

        return pharmacyMedicationMapper.toDTO(pharmacyMedicationRepository.save(item));
    }

    @Override
    public PharmacyMedicationDTO updatePrice(UUID pharmacyId, UUID medicationId, BigDecimal price) {
        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);
        item.setPrice(price);
        return pharmacyMedicationMapper.toDTO(pharmacyMedicationRepository.save(item));
    }

    @Override
    public List<PharmacyMedicationDTO> getPharmacyInventory(UUID pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new RuntimeException("Pharmacy not found");
        }
        return pharmacyMedicationMapper.toDTOList(pharmacyMedicationRepository.findByPharmacyId(pharmacyId));
    }

    @Override
    public PharmacyMedicationDTO getPharmacyMedication(UUID pharmacyId, UUID medicationId) {
        return pharmacyMedicationMapper.toDTO(getLastPharmacyMedication(pharmacyId, medicationId));
    }

    @Override
    public void removeMedicationFromPharmacy(UUID pharmacyId, UUID medicationId) {
        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);
        pharmacyMedicationRepository.delete(item);
    }

    private PharmacyMedication getLastPharmacyMedication(UUID pharmacyId, UUID medicationId) {
        return pharmacyMedicationRepository.findByPharmacyIdAndMedicationId(pharmacyId, medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found in this pharmacy"));
    }
}
