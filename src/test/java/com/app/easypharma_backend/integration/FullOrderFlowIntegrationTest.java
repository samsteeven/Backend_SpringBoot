package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import com.app.easypharma_backend.domain.medication.repository.MedicationRepository;
import com.app.easypharma_backend.domain.order.dto.CreateOrderDTO;
import com.app.easypharma_backend.domain.order.dto.OrderDTO;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.service.interfaces.OrderServiceInterface;
import com.app.easypharma_backend.domain.payment.dto.PaymentRequestDTO;
import com.app.easypharma_backend.domain.payment.entity.Payment;
import com.app.easypharma_backend.domain.payment.entity.PaymentMethod;
import com.app.easypharma_backend.domain.payment.entity.PaymentStatus;
import com.app.easypharma_backend.domain.payment.service.interfaces.PaymentServiceInterface;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.medication.service.interfaces.PharmacyMedicationServiceInterface;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FullOrderFlowIntegrationTest {

        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PharmacyRepository pharmacyRepository;
        @Autowired
        private MedicationRepository medicationRepository;
        @Autowired
        private PharmacyMedicationServiceInterface pharmacyMedicationService;
        @Autowired
        private OrderServiceInterface orderService;
        @Autowired
        private PaymentServiceInterface paymentService;

        private User patient;
        private Pharmacy pharmacy;
        private Medication medication;

        @BeforeEach
        void setup() {
                // 1. Create Users
                User pharmacist = userRepository.save(User.builder()
                                .email("pharma@test.com")
                                .password("test")
                                .firstName("Pharma")
                                .lastName("Cist")
                                .phone("111111111")
                                .role(UserRole.PHARMACY_ADMIN)
                                .isActive(true)
                                .isVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                patient = userRepository.save(User.builder()
                                .email("patient@test.com")
                                .password("test")
                                .firstName("John")
                                .lastName("Doe")
                                .phone("222222222")
                                .role(UserRole.PATIENT)
                                .isActive(true)
                                .isVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // 2. Create Pharmacy
                pharmacy = pharmacyRepository.save(Pharmacy.builder()
                                .user(pharmacist)
                                .name("Pharmacie Test")
                                .licenseNumber("LIC-123")
                                .address("123 Rue Test")
                                .city("Douala")
                                .phone("333333333")
                                .latitude(BigDecimal.valueOf(4.051056))
                                .longitude(BigDecimal.valueOf(9.767868))
                                .status(PharmacyStatus.APPROVED)
                                .licenseDocumentUrl("http://doc.url")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // 3. Create Medication
                medication = medicationRepository.save(Medication.builder()
                                .name("Paracetamol")
                                .therapeuticClass(TherapeuticClass.ANTALGIQUE)
                                .requiresPrescription(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());
        }

        @Test
        void testFullOrderLifecycle() {
                // --- Step 1: Pharmacist adds medication to inventory ---
                BigDecimal price = BigDecimal.valueOf(500);
                Integer initialStock = 10;

                PharmacyMedicationDTO inventoryItem = pharmacyMedicationService.addMedicationToPharmacy(
                                pharmacy.getId(), medication.getId(), price, initialStock);

                assertNotNull(inventoryItem);
                assertEquals(price, inventoryItem.getPrice());
                assertEquals(initialStock, inventoryItem.getStockQuantity());
                assertTrue(inventoryItem.getIsAvailable());

                // --- Step 2: Patient places an order ---
                CreateOrderDTO createOrderDTO = getCreateOrderDTO();

                OrderDTO order = orderService.createOrder(patient.getId(), createOrderDTO);

                assertNotNull(order);
                assertEquals(OrderStatus.PENDING, order.getStatus());
                assertEquals(0, BigDecimal.valueOf(1000.0).compareTo(order.getTotalAmount())); // 500 * 2

                // --- Step 3: Verify Stock is reserved/unchanged pending confirmation ---
                // In current implementation, stock is NOT deducted on Pending, but deducted on
                // CONFIRMED/PAID.
                // Let's verify stock is still 10 (or check validation logic).
                // My implementation checks stock > quantity but doesn't deduct yet.
                PharmacyMedicationDTO stockAfterOrder = pharmacyMedicationService.getPharmacyMedication(
                                pharmacy.getId(),
                                medication.getId());
                assertEquals(10, stockAfterOrder.getStockQuantity());

                // --- Step 4: Process Payment ---
                PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                                .orderId(order.getId())
                                .method(PaymentMethod.MTN_MOMO)
                                .phoneNumber("670000000")
                                .build();
                Payment payment = paymentService.processPayment(paymentRequest);

                assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
                assertNotNull(payment.getTransactionId());

                // --- Step 5: Verify Order Status Updated to PAID ---
                OrderDTO paidOrder = orderService.getOrderById(order.getId());
                assertEquals(OrderStatus.PAID, paidOrder.getStatus());

                // --- Step 6: Verify Stock Deducted ---
                // My implementation deducts stock when "status == PAID" via updateOrderStatus
                // or in processPayment calling updateOrderStatus
                PharmacyMedicationDTO finalStock = pharmacyMedicationService.getPharmacyMedication(pharmacy.getId(),
                                medication.getId());
                assertEquals(8, finalStock.getStockQuantity());

                // --- Step 7: Generate Receipt ---
                byte[] receipt = paymentService.generateReceiptPdf(payment.getId());
                assertNotNull(receipt);
                assertTrue(receipt.length > 0);
        }

        private @NonNull CreateOrderDTO getCreateOrderDTO() {
                int orderQuantity = 2;
                CreateOrderDTO.CreateOrderItemDTO itemDTO = new CreateOrderDTO.CreateOrderItemDTO(medication.getId(),
                                orderQuantity);

                CreateOrderDTO createOrderDTO = new CreateOrderDTO();
                createOrderDTO.setPharmacyId(pharmacy.getId());
                createOrderDTO.setItems(Collections.singletonList(itemDTO));
                createOrderDTO.setDeliveryAddress("Home");
                createOrderDTO.setDeliveryCity("Douala");
                createOrderDTO.setDeliveryPhone("222222222");
                return createOrderDTO;
        }
}
