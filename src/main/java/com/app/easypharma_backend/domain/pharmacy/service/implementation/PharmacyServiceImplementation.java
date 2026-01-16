package com.app.easypharma_backend.domain.pharmacy.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.mapper.PharmacyMapper;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.pharmacy.service.interfaces.PharmacyServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyServiceImplementation implements PharmacyServiceInterface {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapper pharmacyMapper;
    private final UserRepository userRepository;

    @Override
    public List<PharmacyDTO> getAllPharmacies() {
        // Ne retourner que les pharmacies approuvées pour les patients
        return pharmacyMapper.toDTOList(pharmacyRepository.findByStatus(PharmacyStatus.APPROVED));
    }

    @Override
    public List<PharmacyDTO> getAllPharmaciesForAdmin() {
        // Retourner TOUTES les pharmacies sans filtre de statut
        return pharmacyMapper.toDTOList(pharmacyRepository.findAll());
    }

    @Override
    public PharmacyDTO getPharmacyById(@NonNull UUID id) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
        return pharmacyMapper.toDTO(pharmacy);
    }

    @Override
    public PharmacyDTO getPharmacyByLicenseNumber(@NonNull String licenseNumber) {
        Pharmacy pharmacy = pharmacyRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
        return pharmacyMapper.toDTO(pharmacy);
    }

    @Override
    public PharmacyDTO createPharmacy(@NonNull PharmacyDTO pharmacyDTO) {
        // 1. Validate License uniqueness
        if (pharmacyRepository.existsByLicenseNumber(pharmacyDTO.getLicenseNumber())) {
            throw new RuntimeException("License number already exists");
        }

        User user = userRepository.findById(pharmacyDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + pharmacyDTO.getUserId()));

        // 3. Verify Role
        if (user.getRole() != UserRole.PHARMACY_ADMIN) {
            throw new RuntimeException("Only users with role PHARMACY_ADMIN can create a pharmacy");
        }

        // 4. Verify User doesn't already have a pharmacy
        if (pharmacyRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a registered pharmacy");
        }

        Pharmacy pharmacy = pharmacyMapper.toEntity(pharmacyDTO);
        pharmacy.setUser(user);

        Pharmacy saved = pharmacyRepository.save(pharmacy);

        // Update user to link to the new pharmacy
        user.setPharmacy(saved);
        userRepository.save(user);

        return pharmacyMapper.toDTO(saved);
    }

    @Override
    public PharmacyDTO updatePharmacy(@NonNull UUID id, @NonNull PharmacyDTO pharmacyDTO) {
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
    public PharmacyDTO changeStatus(@NonNull UUID id, @NonNull PharmacyStatus status) {
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
    public List<PharmacyDTO> findByCity(@NonNull String city) {
        return pharmacyMapper.toDTOList(pharmacyRepository.findByCity(city));
    }

    @Override
    public List<PharmacyDTO> findByStatus(@NonNull PharmacyStatus status) {
        return pharmacyMapper.toDTOList(pharmacyRepository.findByStatus(status));
    }

    @Override
    public List<PharmacyDTO> findApprovedByCity(@NonNull String city) {
        return pharmacyMapper.toDTOList(pharmacyRepository.findByStatusAndCity(PharmacyStatus.APPROVED, city));
    }

    @Override
    public List<PharmacyDTO> findNearbyPharmacies(@NonNull Double latitude, @NonNull Double longitude,
            @NonNull Double radiusKm) {
        return pharmacyMapper.toDTOList(
                pharmacyRepository.findNearbyPharmacies(latitude, longitude, radiusKm));
    }

    @Override
    public List<PharmacyDTO> searchByName(@NonNull String name) {
        // Ne retourner que les pharmacies approuvées pour les patients
        return pharmacyMapper.toDTOList(pharmacyRepository.searchByNameAndApproved(name));
    }

    @Override
    public void deletePharmacy(@NonNull UUID id) {
        if (!pharmacyRepository.existsById(id)) {
            throw new RuntimeException("Pharmacy not found");
        }
        pharmacyRepository.deleteById(id);
    }

    @Override
    public boolean existsByLicenseNumber(@NonNull String licenseNumber) {
        return pharmacyRepository.existsByLicenseNumber(licenseNumber);
    }
}
