package com.app.easypharma_backend.domain.order.service.interfaces;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.order.dto.PrescriptionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PrescriptionService {
    PrescriptionDTO uploadPrescription(User patient, MultipartFile file);

    List<PrescriptionDTO> getMyPrescriptions(User patient);
}
