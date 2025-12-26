package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.payment.dto.CreatePayoutDTO;
import com.app.easypharma_backend.domain.payment.entity.Payout;
import com.app.easypharma_backend.domain.payment.repository.PayoutRepository;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payouts")
@RequiredArgsConstructor
@Tag(name = "Payouts", description = "Gestion des reversements aux pharmacies")
public class PayoutController {

    private final PayoutRepository payoutRepository;
    private final PharmacyRepository pharmacyRepository;

    @Operation(summary = "Enregistrer un reversement", description = "L'admin note un virement effectué manuellement")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Payout> createPayout(@RequestBody CreatePayoutDTO dto) {
        Payout payout = Payout.builder()
                .pharmacy(pharmacyRepository.findById(dto.getPharmacyId())
                        .orElseThrow(() -> new RuntimeException("Pharmacy not found")))
                .amount(dto.getAmount())
                .transactionReference(dto.getTransactionReference())
                .method(dto.getMethod())
                .notes(dto.getNotes())
                .processedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(payoutRepository.save(payout));
    }

    @Operation(summary = "Historique des reversements", description = "Voir les paiements reçus par une pharmacie")
    @GetMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<List<Payout>> getPharmacyPayouts(@PathVariable UUID pharmacyId) {
        return ResponseEntity.ok(payoutRepository.findByPharmacyIdOrderByProcessedAtDesc(pharmacyId));
    }
}
