package com.app.easypharma_backend.domain.order.service.implementation;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.order.dto.PrescriptionDTO;
import com.app.easypharma_backend.domain.order.entity.Prescription;
import com.app.easypharma_backend.domain.order.repository.PrescriptionRepository;
import com.app.easypharma_backend.domain.order.service.interfaces.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImplementation implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Override
    public PrescriptionDTO uploadPrescription(User patient, MultipartFile file) {
        // FIXME: In a real app, upload 'file' to S3/Cloudinary and get URL.
        // For this demo, we simulate a URL based on the filename or ID.
        String fileName = file.getOriginalFilename();
        String fakeUrl = "https://storage.easypharma.com/prescriptions/" + UUID.randomUUID() + "_" + fileName;

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .photoUrl(fakeUrl)
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
