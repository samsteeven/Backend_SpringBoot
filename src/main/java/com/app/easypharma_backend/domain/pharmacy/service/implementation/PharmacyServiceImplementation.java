package com.app.easypharma_backend.domain.pharmacy.service.implementation;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.mapper.PharmacyMapper;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.pharmacy.service.interfaces.PharmacyServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyServiceImplementation implements PharmacyServiceInterface {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapper pharmacyMapper;

    private final UserRep userRepository;  // ← Ajouter cette dépendance

    @Override
    public List<PharmacyDTO> getAllPharmacies() {
        return pharmacyMapper.toDTOList(pharmacyRepository.findAll());
    }

    @Override
    public PharmacyDTO getPharmacyById(UUID id) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
        return pharmacyMapper.toDTO(pharmacy);
    }

    @Override
    public PharmacyDTO getPharmacyByLicenseNumber(String licenseNumber) {
        Pharmacy pharmacy = pharmacyRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
        return pharmacyMapper.toDTO(pharmacy);
    }

    @Override
    public PharmacyDTO createPharmacy(PharmacyDTO pharmacyDTO) {
        if (pharmacyRepository.existsByLicenseNumber(pharmacyDTO.getLicenseNumber())) {
            throw new RuntimeException("License number already exists");
        }
        Pharmacy pharmacy = pharmacyMapper.toEntity(pharmacyDTO);
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return pharmacyMapper.toDTO(saved);
    }

    @Override
    public PharmacyDTO updatePharmacy(UUID id, PharmacyDTO pharmacyDTO) {
        // 1. Récupérer l'entité depuis le repo
        Pharmacy existing = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        // 2. Appliquer les changements depuis le DTO sur l'entité
        pharmacyMapper.updateEntityFromDTO(pharmacyDTO, existing);

        // 3. Sauvegarder
        Pharmacy saved = pharmacyRepository.save(existing);

        // 4. Retourner le DTO
        return pharmacyMapper.toDTO(saved);
    }

    @Override
    public PharmacyDTO changeStatus(UUID id, PharmacyStatus status) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
        pharmacy.setStatus(status);
        if (status == PharmacyStatus.APPROVED) {
            pharmacy.setValidatedAt(LocalDateTime.now());
        }
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        return pharmacyMapper.toDTO(saved);
    }

    @Override
    public List<PharmacyDTO> findByCity(String city) {
        return pharmacyMapper.toDTOList(pharmacyRepository.findByCity(city));
    }

    @Override
    public List<PharmacyDTO> findByStatus(PharmacyStatus status) {
        return pharmacyMapper.toDTOList(pharmacyRepository.findByStatus(status));
    }

    @Override
    public List<PharmacyDTO> findApprovedByCity(String city) {
        return pharmacyMapper.toDTOList(pharmacyRepository.findByStatusAndCity(PharmacyStatus.APPROVED, city));
    }

    @Override
    public List<PharmacyDTO> findNearbyPharmacies(Double latitude, Double longitude, Double radiusKm) {
        return pharmacyMapper.toDTOList(
                pharmacyRepository.findNearbyPharmacies(latitude, longitude, radiusKm)
        );
    }

    @Override
    public List<PharmacyDTO> searchByName(String name) {
        return pharmacyMapper.toDTOList(pharmacyRepository.searchByName(name));
    }

    @Override
    public void deletePharmacy(UUID id) {
        if (!pharmacyRepository.existsById(id)) {
            throw new RuntimeException("Pharmacy not found");
        }
        pharmacyRepository.deleteById(id);
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return pharmacyRepository.existsByLicenseNumber(licenseNumber);
    }
}
