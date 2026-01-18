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
import com.app.easypharma_backend.domain.audit.service.AuditLogService;
import com.app.easypharma_backend.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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
    private final AuditLogService auditLogService;

    @Override
    public PharmacyMedicationDTO addMedicationToPharmacy(@NonNull UUID pharmacyId, @NonNull UUID medicationId,
            BigDecimal price,
            Integer stock, LocalDate expiryDate) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        Objects.requireNonNull(medicationId, "Medication ID cannot be null");

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
                .expiryDate(expiryDate)
                .isAvailable(stock > 0)
                .build();

        PharmacyMedication savedPharmacyMedication = pharmacyMedicationRepository.save(pharmacyMedication);

        // Log to Audit
        auditLogService.logAction(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentUsername(),
                "ADD_MEDICATION",
                "PharmacyMedication",
                savedPharmacyMedication.getId(),
                java.util.Map.of("medication", medication.getName(), "stock", stock, "price", price),
                null, null, pharmacyId);

        return pharmacyMedicationMapper.toDTO(savedPharmacyMedication);
    }

    @Override
    public PharmacyMedicationDTO updateStock(@NonNull UUID pharmacyId, @NonNull UUID medicationId, Integer quantity) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        Objects.requireNonNull(medicationId, "Medication ID cannot be null");

        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);

        PharmacyMedication saved = pharmacyMedicationRepository.save(item);

        // Log to Audit
        auditLogService.logAction(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentUsername(),
                "UPDATE_STOCK",
                "PharmacyMedication",
                saved.getId(),
                java.util.Map.of("newQuantity", quantity),
                null, null, pharmacyId);

        return pharmacyMedicationMapper.toDTO(saved);
    }

    @Override
    public PharmacyMedicationDTO updatePrice(@NonNull UUID pharmacyId, @NonNull UUID medicationId, BigDecimal price) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        Objects.requireNonNull(medicationId, "Medication ID cannot be null");

        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);
        item.setPrice(price);
        PharmacyMedication saved = pharmacyMedicationRepository.save(item);

        // Log to Audit
        auditLogService.logAction(
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentUsername(),
                "UPDATE_PRICE",
                "PharmacyMedication",
                saved.getId(),
                java.util.Map.of("newPrice", price),
                null, null, pharmacyId);

        return pharmacyMedicationMapper.toDTO(saved);
    }

    @Override
    public PharmacyMedicationDTO updateExpiryDate(@NonNull UUID pharmacyId, @NonNull UUID medicationId,
            LocalDate expiryDate) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");
        Objects.requireNonNull(medicationId, "Medication ID cannot be null");

        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);
        item.setExpiryDate(expiryDate);
        return pharmacyMedicationMapper.toDTO(pharmacyMedicationRepository.save(item));
    }

    @Override
    public List<PharmacyMedicationDTO> getPharmacyInventory(@NonNull UUID pharmacyId) {
        Objects.requireNonNull(pharmacyId, "Pharmacy ID cannot be null");

        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new RuntimeException("Pharmacy not found");
        }
        return pharmacyMedicationMapper.toDTOList(pharmacyMedicationRepository.findByPharmacyId(pharmacyId));
    }

    @Override
    public PharmacyMedicationDTO getPharmacyMedication(@NonNull UUID pharmacyId, @NonNull UUID medicationId) {
        return pharmacyMedicationMapper.toDTO(getLastPharmacyMedication(pharmacyId, medicationId));
    }

    @Override
    public void removeMedicationFromPharmacy(@NonNull UUID pharmacyId, @NonNull UUID medicationId) {
        PharmacyMedication item = getLastPharmacyMedication(pharmacyId, medicationId);
        pharmacyMedicationRepository.delete(item);
    }

    @Override
    @Transactional
    public int importMedications(@NonNull UUID pharmacyId, @NonNull String csvContent) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        String[] lines = csvContent.split("\n");
        int count = 0;
        for (String line : lines) {
            try {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(",");
                if (parts.length < 4)
                    continue;

                String name = parts[0].trim();
                String dosage = parts[1].trim();
                BigDecimal price = new BigDecimal(parts[2].trim());
                Integer stock = Integer.parseInt(parts[3].trim());
                LocalDate expiryDate = (parts.length > 4 && !parts[4].trim().isEmpty())
                        ? LocalDate.parse(parts[4].trim())
                        : null;

                Optional<Medication> medicationOpt = medicationRepository.findByNameAndDosage(name, dosage);
                if (medicationOpt.isPresent()) {
                    Medication med = medicationOpt.get();
                    Optional<PharmacyMedication> existing = pharmacyMedicationRepository
                            .findByPharmacyIdAndMedicationId(pharmacyId, med.getId());
                    if (existing.isPresent()) {
                        PharmacyMedication pm = existing.get();
                        pm.setStockQuantity(pm.getStockQuantity() + stock);
                        pm.setPrice(price);
                        if (expiryDate != null)
                            pm.setExpiryDate(expiryDate);
                        pharmacyMedicationRepository.save(pm);
                    } else {
                        PharmacyMedication pm = PharmacyMedication.builder()
                                .pharmacy(pharmacy)
                                .medication(med)
                                .price(price)
                                .stockQuantity(stock)
                                .expiryDate(expiryDate)
                                .isAvailable(stock > 0)
                                .build();
                        pharmacyMedicationRepository.save(pm);
                    }
                    count++;
                }
            } catch (Exception e) {
                // Log and continue with next line
                continue;
            }
        }
        return count;
    }

    private PharmacyMedication getLastPharmacyMedication(@NonNull UUID pharmacyId, @NonNull UUID medicationId) {
        return pharmacyMedicationRepository.findByPharmacyIdAndMedicationId(pharmacyId, medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found in this pharmacy"));
    }
}