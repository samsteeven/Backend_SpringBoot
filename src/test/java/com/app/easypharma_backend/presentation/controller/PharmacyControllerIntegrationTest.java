package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PharmacyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setup() {
        // Create PHARMACY_ADMIN user
        User admin = userRepository.save(User.builder()
                .email("pharma_admin@test.com")
                .password("test")
                .firstName("Pharma")
                .lastName("Admin")
                .phone("111111111")
                .role(UserRole.PHARMACY_ADMIN)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        jwtToken = jwtService.generateToken(admin);
    }

    @Test
    void createPharmacy_ShouldReturnCreated() throws Exception {
        PharmacyDTO pharmacyDTO = new PharmacyDTO();
        pharmacyDTO.setName("Test Pharmacy");
        pharmacyDTO.setLicenseNumber("LIC-12345");
        pharmacyDTO.setAddress("123 Test St");
        pharmacyDTO.setCity("Douala");
        pharmacyDTO.setPhone("222222222");
        pharmacyDTO.setLatitude(BigDecimal.valueOf(4.0));
        pharmacyDTO.setLongitude(BigDecimal.valueOf(9.7));

        MockMultipartFile pharmacyPart = new MockMultipartFile(
                "pharmacy",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(pharmacyDTO));

        MockMultipartFile licensePart = new MockMultipartFile(
                "licenseDocument",
                "license.pdf",
                "application/pdf",
                "dummy content".getBytes());

        mockMvc.perform(multipart("/api/v1/pharmacies")
                .file(pharmacyPart)
                .file(licensePart)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }
}
