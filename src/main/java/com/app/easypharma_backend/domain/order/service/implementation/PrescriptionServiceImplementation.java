package com.app.easypharma_backend.domain.order.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.order.dto.PrescriptionDTO;
import com.app.easypharma_backend.domain.order.entity.Prescription;
import com.app.easypharma_backend.domain.order.repository.PrescriptionRepository;
import com.app.easypharma_backend.domain.order.service.interfaces.PrescriptionService;
import com.app.easypharma_backend.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImplementation implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final FileStorageService fileStorageService;

    @Override
    public PrescriptionDTO uploadPrescription(User patient, MultipartFile file) {
        String photoUrl = fileStorageService.storeFile(file, "prescriptions");

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .photoUrl(photoUrl)
                .build();

        Prescription saved = prescriptionRepository.save(prescription);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getMyPrescriptions(User patient) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PrescriptionDTO mapToDTO(Prescription p) {
        return PrescriptionDTO.builder()
                .id(p.getId())
                .photoUrl(p.getPhotoUrl())
                .status(p.getStatus())
                .pharmacistComment(p.getPharmacistComment())
                .createdAt(p.getCreatedAt())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .build();
    }
}
