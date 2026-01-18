package com.app.easypharma_backend.domain.order.service.implementation;

import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderItem;
import com.app.easypharma_backend.domain.order.service.interfaces.PdfServiceInterface;
import com.app.easypharma_backend.domain.payment.entity.Payment;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Service
@Slf4j
public class PdfServiceImplementation implements PdfServiceInterface {

    @Override
    public byte[] generateReceiptPdf(@NonNull Payment payment) {
        Objects.requireNonNull(payment, "Payment cannot be null");
        Order order = payment.getOrder();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("EASYPHARMA - REÇU DE PAIEMENT").setFontSize(20).setBold());
            document.add(new Paragraph("N° Transaction : " + payment.getTransactionId()));
            document.add(
                    new Paragraph("Date : " + (payment.getPaidAt() != null ? payment.getPaidAt().toString() : "N/A")));
            document.add(new Paragraph("Pharmacie : " + order.getPharmacy().getName()));
            document.add(new Paragraph(
                    "Patient : " + order.getPatient().getFirstName() + " " + order.getPatient().getLastName()));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 2, 2 }));
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell("Médicament");
            table.addHeaderCell("Quantité");
            table.addHeaderCell("Prix Unitaire");

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getMedication().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getUnitPrice().toString() + " FCFA");
            }

            document.add(table);

            if (order.getDeliveryFee() != null) {
                document.add(new Paragraph("Frais de livraison : " + order.getDeliveryFee() + " FCFA"));
            }

            document.add(new Paragraph("\nTOTAL PAYÉ : " + payment.getAmount() + " FCFA").setBold());
            document.add(new Paragraph("Méthode de paiement : " + payment.getPaymentMethod().getDisplayName()));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating receipt PDF", e);
            throw new RuntimeException("Erreur lors de la génération du reçu PDF", e);
        }
    }

    @Override
    public byte[] generateInvoicePdf(@NonNull Order order) {
        Objects.requireNonNull(order, "Order cannot be null");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("EASYPHARMA - FACTURE").setFontSize(20).setBold());
            document.add(new Paragraph("N° Commande : " + order.getOrderNumber()));
            document.add(new Paragraph("Date : " + order.getCreatedAt().toString()));
            document.add(new Paragraph("Pharmacie : " + order.getPharmacy().getName()));
            document.add(new Paragraph(
                    "Patient : " + order.getPatient().getFirstName() + " " + order.getPatient().getLastName()));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 2, 2 }));
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell("Médicament");
            table.addHeaderCell("Quantité");
            table.addHeaderCell("Prix Unitaire");

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getMedication().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getUnitPrice().toString() + " FCFA");
            }

            document.add(table);

            if (order.getDeliveryFee() != null) {
                document.add(new Paragraph("Frais de livraison : " + order.getDeliveryFee() + " FCFA"));
            }

            document.add(new Paragraph("\nTOTAL À RÉGLER : " + order.getTotalAmount() + " FCFA").setBold());
            document.add(new Paragraph("Statut : " + order.getStatus().name()));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating invoice PDF", e);
            throw new RuntimeException("Erreur lors de la génération de la facture PDF", e);
        }
    }
}
