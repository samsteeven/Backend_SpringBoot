package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.config.TestMailConfiguration;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestMailConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        // Create Admin
        userRepository.save(User.builder()
                .email("admin_dash@test.com")
                .password("test")
                .firstName("Super")
                .lastName("Admin")
                .phone("111111999")
                .role(UserRole.SUPER_ADMIN)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Data for Stats
        User pharmacist = userRepository.save(User.builder()
                .email("pharma_dash@test.com")
                .password("test")
                .firstName("Pharma")
                .lastName("Dash")
                .phone("111111888")
                .role(UserRole.PHARMACY_ADMIN)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Pharmacy pharmacy = pharmacyRepository.save(Pharmacy.builder()
                .user(pharmacist)
                .name("Pharmacie Dash")
                .licenseNumber("LIC-DASH")
                .address("Akwa")
                .city("Douala")
                .phone("333333555")
                .latitude(BigDecimal.valueOf(4.05))
                .longitude(BigDecimal.valueOf(9.70))
                .status(PharmacyStatus.APPROVED)
                .licenseDocumentUrl("http://doc.url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        orderRepository.save(Order.builder()
                .pharmacy(pharmacy)
                .patient(pharmacist) // Self order for simplicity or mock patient
                .status(OrderStatus.PAID)
                .totalAmount(BigDecimal.valueOf(5000))
                .orderNumber("ORD-DASH-001")
                .deliveryAddress("Home")
                .deliveryCity("Douala")
                .deliveryPhone("222222333")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @WithMockUser(username = "admin_dash@test.com", roles = { "SUPER_ADMIN" })
    void getGlobalStats_shouldReturnData() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/stats"))
                .andExpect(status().isOk())
                // Assuming the DTO has totalRevenue, totalOrders etc.
                // We check if it returns JSON object
                .andExpect(jsonPath("$.totalRevenue").exists())
                .andExpect(jsonPath("$.totalOrders").exists());
    }
}
