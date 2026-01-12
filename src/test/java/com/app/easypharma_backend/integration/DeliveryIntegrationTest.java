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
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DeliveryIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    private User pharmacyAdmin;
    private User deliveryPerson;
    private Pharmacy pharmacy;
    private Order order;

    @BeforeEach
    void setup() {
        // 1. Create Pharmacy Admin
        pharmacyAdmin = userRepository.save(User.builder()
                .email("pharma_delivery@test.com")
                .password("test")
                .firstName("Pharma")
                .lastName("Delivery")
                .phone("111111113")
                .role(UserRole.PHARMACY_ADMIN)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 2. Create Delivery Person
        deliveryPerson = userRepository.save(User.builder()
                .email("courier@test.com")
                .password("test")
                .firstName("John")
                .lastName("Courier")
                .phone("111111114")
                .role(UserRole.DELIVERY)
                .city("Douala")
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 3. Create Pharmacy
        pharmacy = pharmacyRepository.save(Pharmacy.builder()
                .user(pharmacyAdmin)
                .name("Pharmacie Delivery")
                .licenseNumber("LIC-DELIVERY")
                .address("Akwa")
                .city("Douala")
                .phone("333333335")
                .latitude(BigDecimal.valueOf(4.05))
                .longitude(BigDecimal.valueOf(9.70))
                .status(PharmacyStatus.APPROVED)
                .licenseDocumentUrl("http://doc.url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 4. Create Patient
        User patient = userRepository.save(User.builder()
                .email("patient_delivery@test.com")
                .password("test")
                .firstName("Patient")
                .lastName("Delivery")
                .phone("111111115")
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 5. Create Order (Ready for delivery)
        order = orderRepository.save(Order.builder()
                .pharmacy(pharmacy)
                .patient(patient)
                .status(OrderStatus.PAID)
                .totalAmount(BigDecimal.valueOf(1000))
                .orderNumber("ORD-DELIVERY-001")
                .deliveryAddress("Bonanjo")
                .deliveryCity("Douala")
                .deliveryPhone("222222222")
                .deliveryLatitude(BigDecimal.valueOf(4.04))
                .deliveryLongitude(BigDecimal.valueOf(9.69))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @WithMockUser
    void assignDelivery_shouldCreateDelivery() throws Exception {
        // Need to authenticate as Pharmacy Admin/Employee to assign?
        // Let's assume the endpoint is accessible or we mock user well
        // Actually the endpoint to assign delivery typically takes deliveryPersonId

        // Note: Check DeliveryController logic. Assuming POST
        // /api/v1/deliveries/orders/{orderId}/assign

        // For now, let's test creating a delivery via service or controller if
        // accessible
        // Or specific endpoint: POST /api/v1/deliveries/assign

        // Let's try to simulate the flow:
        // 1. Assign delivery
        // 2. Update status

        // Assuming endpoint /api/v1/pharmacy/orders/{orderId}/assign-delivery
        // Payload: { deliveryPersonId: "..." }

        mockMvc.perform(post("/api/v1/deliveries/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new java.util.HashMap<String, String>() {
                    {
                        put("orderId", order.getId().toString());
                        put("deliveryPersonId", deliveryPerson.getId().toString());
                    }
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.orderId", is(order.getId().toString())));
    }
}
