package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.order.dto.CreateReviewDTO;
import com.app.easypharma_backend.domain.order.dto.ReviewDTO;
import com.app.easypharma_backend.domain.order.entity.ReviewStatus;
import com.app.easypharma_backend.domain.order.service.implementation.ReviewServiceImplementation;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Gestion des avis et notations")
public class ReviewController {

    private final ReviewServiceImplementation reviewService;
    private final UserRepository userRepository;

    @Operation(summary = "Laisser un avis", description = "Un patient laisse un avis sur une commande livrée")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewDTO> createReview(@RequestBody @Valid CreateReviewDTO dto) {
        UUID patientId = getCurrentUserId();
        return ResponseEntity.ok(reviewService.createReview(patientId, dto));
    }

    @Operation(summary = "Lister les avis d'une pharmacie", description = "Récupère les avis approuvés pour une pharmacie")
    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<ReviewDTO>> getPharmacyReviews(@PathVariable @NonNull UUID pharmacyId) {
        return ResponseEntity.ok(reviewService.getPharmacyReviews(pharmacyId));
    }

    @Operation(summary = "Modérer un avis (Admin)", description = "Approuver ou rejeter un avis")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ReviewDTO> moderateReview(
            @PathVariable @NonNull UUID id,
            @RequestParam @NonNull ReviewStatus status) {
        return ResponseEntity.ok(reviewService.moderateReview(id, status));
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
