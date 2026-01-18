package com.app.easypharma_backend.domain.payment.service.implementation;

import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.order.service.interfaces.OrderServiceInterface;
import com.app.easypharma_backend.domain.payment.dto.PaymentRequestDTO;
import com.app.easypharma_backend.domain.payment.entity.Payment;
import com.app.easypharma_backend.domain.payment.entity.PaymentStatus;
import com.app.easypharma_backend.domain.payment.repository.PaymentRepository;
import com.app.easypharma_backend.domain.payment.service.interfaces.PaymentServiceInterface;
import com.app.easypharma_backend.domain.notification.entity.NotificationType;
import com.app.easypharma_backend.domain.notification.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImplementation implements PaymentServiceInterface {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final com.app.easypharma_backend.domain.auth.repository.UserRepository userRepository;
    private final OrderServiceInterface orderService; // To update status
    private final NotificationService notificationService;
    private final com.app.easypharma_backend.domain.order.service.interfaces.PdfServiceInterface pdfService;

    @Override
    @Transactional
    public Payment processPayment(@NonNull PaymentRequestDTO request) {
        Objects.requireNonNull(request, "Payment request cannot be null");

        // 0. Get Authenticated User
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String currentEmail = auth.getName();
        com.app.easypharma_backend.domain.auth.entity.User authUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

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

            // SECURITY: Cross-check ownership
            if (!order.getPatient().getId().equals(authUser.getId())) {
                throw new com.app.easypharma_backend.infrastructure.exception.ValidationException(
                        "Accès refusé : La commande " + order.getOrderNumber() + " ne vous appartient pas");
            }

            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.DELIVERED) {
                throw new RuntimeException("Order " + order.getOrderNumber() + " is already paid");
            }
            ordersToPay.add(order);
            totalAmount = totalAmount.add(order.getTotalAmount());
        }

        // Mock Payment Logic (One atomic payment for the total)
        boolean isSuccess = true;

        // Structured Transaction ID: TXN-TIME-RANDOM
        String sharedTransactionId = "TXN-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

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

                // Send Multi-channel Notification
                String title = "Paiement réussi";
                String message = String.format(
                        "Votre paiement pour la commande %s d'un montant de %s FCFA a été traité avec succès.",
                        order.getOrderNumber(), order.getTotalAmount());

                notificationService.sendInAppNotification(order.getPatient(), title, message, NotificationType.PAYMENT);
                notificationService.sendSms(order.getPatient().getPhone(), message);
                notificationService.sendEmail(order.getPatient().getEmail(), title, message);
            }
        }

        return lastPayment;
    }

    @Override
    public byte[] generateReceiptPdf(@NonNull UUID paymentId) {
        Objects.requireNonNull(paymentId, "Payment ID cannot be null");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot generate receipt for failed/pending payment");
        }

        return pdfService.generateReceiptPdf(payment);
    }
}
