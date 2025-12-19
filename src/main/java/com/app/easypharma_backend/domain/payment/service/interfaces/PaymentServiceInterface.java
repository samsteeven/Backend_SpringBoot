package com.app.easypharma_backend.domain.payment.service.interfaces;

import com.app.easypharma_backend.domain.payment.dto.PaymentRequestDTO;
import com.app.easypharma_backend.domain.payment.entity.Payment;

import java.util.UUID;

public interface PaymentServiceInterface {

    /**
     * Process a payment request (Mocked for MoMo/OM).
     */
    Payment processPayment(PaymentRequestDTO request);

    /**
     * Generate a PDF receipt for a successful payment.
     * Returns the byte array of the PDF.
     */
    byte[] generateReceiptPdf(UUID paymentId);
}
