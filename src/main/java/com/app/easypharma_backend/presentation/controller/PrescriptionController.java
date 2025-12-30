package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.usecase.GetUserProfileUseCase;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.order.dto.PrescriptionDTO;
import com.app.easypharma_backend.domain.order.service.interfaces.PrescriptionService;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Gestion des ordonnances (Upload photo)")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Operation(summary = "Téléverser une ordonnance", description = "Envoie une photo d'ordonnance.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PrescriptionDTO> uploadPrescription(
            @Parameter(description = "Fichier image de l'ordonnance", required = true) @RequestPart("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {

        User patient = getUserFromHeader(authHeader);
        PrescriptionDTO result = prescriptionService.uploadPrescription(patient, file);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Mes ordonnances", description = "Liste l'historique des ordonnances envoyées.")
    @GetMapping("/my-prescriptions")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<List<PrescriptionDTO>> getMyPrescriptions(
            @RequestHeader("Authorization") String authHeader) {

        User patient = getUserFromHeader(authHeader);
        return ResponseEntity.ok(prescriptionService.getMyPrescriptions(patient));
    }

    private User getUserFromHeader(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
