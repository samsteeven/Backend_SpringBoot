package com.app.easypharma_backend.domain.order.service.interfaces;

import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.payment.entity.Payment;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface PdfServiceInterface {
    /**
     * Génère un reçu de paiement au format PDF
     * 
     * @param payment Le paiement effectué
     * @return Le contenu du PDF en octets
     */
    byte[] generateReceiptPdf(@NonNull Payment payment);

    /**
     * Génère une facture client au format PDF
     * 
     * @param order La commande concernée
     * @return Le contenu du PDF en octets
     */
    byte[] generateInvoicePdf(@NonNull Order order);
}
