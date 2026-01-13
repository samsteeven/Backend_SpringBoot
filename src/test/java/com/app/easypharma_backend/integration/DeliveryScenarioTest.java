package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import com.app.easypharma_backend.domain.delivery.entity.DeliveryStatus;
import com.app.easypharma_backend.domain.delivery.repository.DeliveryRepository;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class DeliveryScenarioTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PharmacyRepository pharmacyRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private DeliveryRepository deliveryRepository;

        private Delivery delivery;
        private User courier;

        @BeforeEach
        void setup() {
                // 1. Courier
                courier = userRepository.save(User.builder()
                                .email("courier_scen@test.com")
                                .password("test")
                                .firstName("Fast")
                                .lastName("Deliv")
                                .phone("444444444")
                                .role(UserRole.DELIVERY)
                                .isActive(true)
                                .isVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // 2. Pharmacy Owner & Pharmacy
                User pharmacist = userRepository.save(User.builder()
                                .email("pharma_deliv@test.com")
                                .password("test")
                                .firstName("Pharma")
                                .lastName("Deliv")
                                .phone("111111111")
                                .role(UserRole.PHARMACY_ADMIN)
                                .build());

                Pharmacy pharmacy = pharmacyRepository.save(Pharmacy.builder()
                                .user(pharmacist)
                                .name("Pharmacie Deliv")
                                .licenseNumber("LIC-DELIV")
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

                // 3. Order
                User patient = userRepository.save(User.builder()
                                .email("patient_deliv@test.com")
                                .password("test")
                                .firstName("Patient")
                                .lastName("Deliv")
                                .phone("222222222")
                                .role(UserRole.PATIENT)
                                .build());

                Order order = orderRepository.save(Order.builder()
                                .pharmacy(pharmacy)
                                .patient(patient)
                                .status(OrderStatus.CONFIRMED)
                                .totalAmount(BigDecimal.valueOf(2000))
                                .orderNumber("ORD-DELIV-001")
                                .deliveryAddress("Bonamoussadi")
                                .deliveryCity("Douala")
                                .deliveryPhone("699999999")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // 4. Delivery Assigned
                delivery = deliveryRepository.save(Delivery.builder()
                                .order(order)
                                .deliveryPerson(courier)
                                .status(DeliveryStatus.ASSIGNED)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());
        }

        @Test
        @WithMockUser(username = "courier_scen@test.com", roles = { "DELIVERY" })
        void testCourierFlow() throws Exception {
                // --- 1. View Ongoing Deliveries ---
                mockMvc.perform(get("/api/v1/deliveries/my-deliveries/ongoing"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$.data[0].deliveryId", is(delivery.getId().toString())));

                // --- 2. Pick Up Package ---
                mockMvc.perform(patch("/api/v1/deliveries/" + delivery.getId() + "/status")
                                .param("status", "PICKED_UP"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status", is("PICKED_UP")));

                // --- 3. Update Location (Simulated) ---
                mockMvc.perform(patch("/api/v1/deliveries/" + delivery.getId() + "/location")
                                .param("latitude", "4.06")
                                .param("longitude", "9.71"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.currentLatitude", is(4.06)))
                                .andExpect(jsonPath("$.data.currentLongitude", is(9.71)));

                // --- 4. Sustain Delivery (Add proof) -> DELIVERED ---
                mockMvc.perform(patch("/api/v1/deliveries/" + delivery.getId() + "/proof")
                                .param("photoUrl", "http://proof.url/photo.jpg"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status", is("DELIVERED")))
                                .andExpect(jsonPath("$.data.photoProofUrl", is("http://proof.url/photo.jpg")));
        }
}
