package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import com.app.easypharma_backend.domain.medication.repository.MedicationRepository;
import com.app.easypharma_backend.domain.order.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PharmacistScenarioTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PharmacyRepository pharmacyRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private MedicationRepository medicationRepository;

        @Autowired
        private com.app.easypharma_backend.domain.medication.repository.PharmacyMedicationRepository pharmacyMedicationRepository;

        private User pharmacist;
        private Pharmacy pharmacy;
        private Order order;
        private User courier;

        @BeforeEach
        void setup() {
                // 1. Pharmacist
                pharmacist = userRepository.save(User.builder()
                                .email("pharmacist_flow@test.com")
                                .password("test")
                                .firstName("Pharma")
                                .lastName("Flow")
                                .phone("111111111")
                                .role(UserRole.PHARMACY_ADMIN)
                                .isActive(true)
                                .isVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                pharmacy = pharmacyRepository.save(Pharmacy.builder()
                                .user(pharmacist)
                                .name("Pharmacie Flow")
                                .licenseNumber("LIC-FLOW")
                                .address("Akwa")
                                .city("Douala")
                                .phone("333333333")
                                .latitude(BigDecimal.valueOf(4.05))
                                .longitude(BigDecimal.valueOf(9.70))
                                .status(PharmacyStatus.APPROVED)
                                .licenseDocumentUrl("http://doc.url")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // LINK BACK: Important for controllers that fetch Pharmacy via User
                pharmacist.setPharmacy(pharmacy);
                userRepository.saveAndFlush(pharmacist);

                // Verify association in memory for the current transaction
                if (pharmacist.getPharmacy() == null) {
                        throw new RuntimeException(
                                        "Setup failure: pharmacist.getPharmacy() is still null after saveAndFlush");
                }

                // 3. Courier (Delivery Person)
                courier = userRepository.save(User.builder()
                                .email("courier_flow@test.com")
                                .password("test")
                                .firstName("Fast")
                                .lastName("Courier")
                                .phone("555555555")
                                .role(UserRole.DELIVERY)
                                .isActive(true)
                                .isVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // LINK COURIER to pharmacy as well
                courier.setPharmacy(pharmacy);
                userRepository.saveAndFlush(courier);

                // 4. Order (PAID status)
                User patient = userRepository.save(User.builder()
                                .email("patient_flow@test.com")
                                .password("test")
                                .firstName("Patient")
                                .lastName("Flow")
                                .phone("777777777")
                                .role(UserRole.PATIENT)
                                .build());

                Medication medication = medicationRepository.save(Medication.builder()
                                .name("Doliprane")
                                .therapeuticClass(TherapeuticClass.ANTALGIQUE)
                                .requiresPrescription(false)
                                .build());

                // Setup stock for deduction
                pharmacyMedicationRepository
                                .save(com.app.easypharma_backend.domain.medication.entity.PharmacyMedication.builder()
                                                .pharmacy(pharmacy)
                                                .medication(medication)
                                                .price(BigDecimal.valueOf(500))
                                                .stockQuantity(10)
                                                .isAvailable(true)
                                                .build());

                order = orderRepository.save(Order.builder()
                                .pharmacy(pharmacy)
                                .patient(patient)
                                .status(OrderStatus.PENDING)
                                .totalAmount(BigDecimal.valueOf(1000))
                                .orderNumber("ORD-FLOW-001")
                                .deliveryAddress("Home")
                                .deliveryCity("Douala")
                                .deliveryPhone("222222333")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                OrderItem item = OrderItem.builder()
                                .order(order)
                                .medication(medication)
                                .quantity(2)
                                .unitPrice(BigDecimal.valueOf(500))
                                .build();

                order.getItems().add(item);
                orderRepository.save(order);
        }

        @Test
        @WithMockUser(username = "pharmacist_flow@test.com", roles = { "PHARMACY_ADMIN" })
        void testPharmacistFlow() throws Exception {
                // --- 1. Get Pharmacy Orders ---
                mockMvc.perform(get("/api/v1/orders/pharmacy-orders/" + pharmacy.getId()))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$[0].id", is(order.getId().toString())));

                // --- 2. Update Status to CONFIRMED ---
                mockMvc.perform(patch("/api/v1/orders/" + order.getId() + "/status")
                                .param("status", "CONFIRMED"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("CONFIRMED")));

                // 3. Assign delivery
                mockMvc.perform(post("/api/v1/deliveries/assign")
                                .param("orderId", order.getId().toString())
                                .param("courierId", courier.getId().toString()))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.orderId", is(order.getId().toString())))
                                .andExpect(jsonPath("$.data.deliveryPersonId", is(courier.getId().toString())))
                                .andExpect(jsonPath("$.data.status", is("ASSIGNED")));
        }
}
