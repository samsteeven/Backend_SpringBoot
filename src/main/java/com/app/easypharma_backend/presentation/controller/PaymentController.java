package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.payment.dto.PaymentRequestDTO;
import com.app.easypharma_backend.domain.payment.entity.Payment;
import com.app.easypharma_backend.domain.payment.service.interfaces.PaymentServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Gestion des paiements et reçus")
public class PaymentController {

    private final PaymentServiceInterface paymentService;

    @Operation(summary = "Effectuer un paiement", description = "Simule un paiement Mobile Money (MTN/Orange)")
    @PostMapping("/process")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Payment> processPayment(@RequestBody @NonNull PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @Operation(summary = "Télécharger le reçu", description = "Génère un PDF pour un paiement réussi")
    @GetMapping("/{paymentId}/receipt")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable @NonNull UUID paymentId) {
        Objects.requireNonNull(paymentId, "Payment ID cannot be null");
        byte[] pdfBytes = paymentService.generateReceiptPdf(paymentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_" + paymentId + ".pdf")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_PDF))
                .body(pdfBytes);
    }
}
