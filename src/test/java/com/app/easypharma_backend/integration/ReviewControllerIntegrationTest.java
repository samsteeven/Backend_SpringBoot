package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.entity.Review;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.order.repository.ReviewRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User patient;
    private Pharmacy pharmacy;
    private Order order;

    @BeforeEach
    void setup() {
        User pharmacist = userRepository.save(User.builder()
                .email("pharma_review@test.com")
                .password("test")
                .firstName("Pharma")
                .lastName("Review")
                .phone("111111222")
                .role(UserRole.PHARMACY_ADMIN)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        pharmacy = pharmacyRepository.save(Pharmacy.builder()
                .user(pharmacist)
                .name("Pharmacie Review")
                .licenseNumber("LIC-REVIEW")
                .address("Akwa")
                .city("Douala")
                .phone("333333444")
                .latitude(BigDecimal.valueOf(4.05))
                .longitude(BigDecimal.valueOf(9.70))
                .status(PharmacyStatus.APPROVED)
                .licenseDocumentUrl("http://doc.url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        patient = userRepository.save(User.builder()
                .email("patient_review@test.com")
                .password("test")
                .firstName("John")
                .lastName("Doe")
                .phone("222222333")
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        order = orderRepository.save(Order.builder()
                .pharmacy(pharmacy)
                .patient(patient)
                .status(OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(1000))
                .orderNumber("ORD-REVIEW-001")
                .deliveryAddress("Home")
                .deliveryCity("Douala")
                .deliveryPhone("222222333")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @WithMockUser(username = "patient_review@test.com", roles = { "PATIENT" })
    void createReview_shouldReturnOk() throws Exception {
        // Authenticate logic needed if looking up by logged in user
        // Assuming endpoint takes reviewDTO

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {
                    {
                        put("orderId", order.getId().toString());
                        put("pharmacyRating", 5);
                        put("pharmacyComment", "Great service!");
                    }
                })))
                .andExpect(status().isOk());
    }
}
