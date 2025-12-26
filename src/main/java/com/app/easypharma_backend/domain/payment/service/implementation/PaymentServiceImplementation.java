package com.app.easypharma_backend.domain.payment.service.implementation;

import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderItem;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.order.service.interfaces.OrderServiceInterface;
import com.app.easypharma_backend.domain.payment.dto.PaymentRequestDTO;
import com.app.easypharma_backend.domain.payment.entity.Payment;
import com.app.easypharma_backend.domain.payment.entity.PaymentStatus;
import com.app.easypharma_backend.domain.payment.repository.PaymentRepository;
import com.app.easypharma_backend.domain.payment.service.interfaces.PaymentServiceInterface;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImplementation implements PaymentServiceInterface {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderServiceInterface orderService; // To update status

    @Override
    public Payment processPayment(@NonNull PaymentRequestDTO request) {
        Objects.requireNonNull(request, "Payment request cannot be null");

        // Support backward compatibility
        java.util.List<UUID> targetOrderIds = request.getOrderIds();
        if (targetOrderIds == null || targetOrderIds.isEmpty()) {
            if (request.getOrderId() != null) {
                targetOrderIds = java.util.Collections.singletonList(request.getOrderId());
            } else {
                throw new RuntimeException("No order IDs provided");
            }
        }

        java.util.List<Order> ordersToPay = new java.util.ArrayList<>();
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

        // 1. Validate all orders
        for (UUID id : targetOrderIds) {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + id));

            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.DELIVERED) {
                throw new RuntimeException("Order " + order.getOrderNumber() + " is already paid");
            }
            ordersToPay.add(order);
            totalAmount = totalAmount.add(order.getTotalAmount());
        }

        // Mock Payment Logic (One atomic payment for the total)
        // In a real app, calls MTN/Orange API here with totalAmount.
        boolean isSuccess = true;
        String sharedTransactionId = UUID.randomUUID().toString();

        Payment lastPayment = null;

        // 2. Create Payment records for each order (linking them to same transaction)
        for (Order order : ordersToPay) {
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(request.getMethod())
                    .phoneNumber(request.getPhoneNumber())
                    .amount(order.getTotalAmount()) // Each record tracks its own amount
                    .status(PaymentStatus.SUCCESS)
                    .transactionId(sharedTransactionId) // Shared transaction ID
                    .paidAt(LocalDateTime.now())
                    .build();

            lastPayment = paymentRepository.save(payment);

            if (isSuccess) {
                orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);
            }
        }

        return lastPayment; // Return the last payment record (or we could return a list)
    }

    @Override
    public byte[] generateReceiptPdf(@NonNull UUID paymentId) {
        Objects.requireNonNull(paymentId, "Payment ID cannot be null");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot generate receipt for failed/pending payment");
        }

        Order order = payment.getOrder();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("EASYPHARMA - RECEIPT").setFontSize(20).setBold());
            document.add(new Paragraph("Payment ID: " + payment.getTransactionId()));
            document.add(new Paragraph("Date: " + payment.getPaidAt().toString()));
            document.add(new Paragraph("Pharmacy: " + order.getPharmacy().getName()));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 2, 2 }));
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell("Item");
            table.addHeaderCell("Qty");
            table.addHeaderCell("Price");

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getMedication().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getUnitPrice().toString());
            }

            document.add(table);
            document.add(new Paragraph("\nTotal: " + order.getTotalAmount() + " FCFA").setBold());
            document.add(new Paragraph("Payment Method: " + payment.getPaymentMethod().getDisplayName()));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF receipt", e);
        }
    }
}
