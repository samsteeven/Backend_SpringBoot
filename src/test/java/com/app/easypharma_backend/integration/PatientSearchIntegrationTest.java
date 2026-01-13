package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import com.app.easypharma_backend.domain.medication.repository.MedicationRepository;
import com.app.easypharma_backend.domain.medication.service.interfaces.PharmacyMedicationServiceInterface;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PatientSearchIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PharmacyRepository pharmacyRepository;

        @Autowired
        private MedicationRepository medicationRepository;

        @Autowired
        private PharmacyMedicationServiceInterface pharmacyMedicationService;

        @BeforeEach
        void setup() {
                // 1. Create Data
                User pharmacist = userRepository.save(User.builder()
                                .email("pharma_search@test.com")
                                .password("test")
                                .firstName("Pharma")
                                .lastName("Search")
                                .phone("111111112")
                                .role(UserRole.PHARMACY_ADMIN)
                                .isActive(true)
                                .isVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                Pharmacy pharmacy = pharmacyRepository.save(Pharmacy.builder()
                                .user(pharmacist)
                                .name("Pharmacie Centrale")
                                .licenseNumber("LIC-SEARCH")
                                .address("Akwa")
                                .city("Douala")
                                .phone("333333334")
                                .latitude(BigDecimal.valueOf(4.05))
                                .longitude(BigDecimal.valueOf(9.70))
                                .status(PharmacyStatus.APPROVED)
                                .licenseDocumentUrl("http://doc.url")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                Medication med1 = medicationRepository.save(Medication.builder()
                                .name("Doliprane 1000")
                                .therapeuticClass(TherapeuticClass.ANTALGIQUE)
                                .requiresPrescription(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                Medication med2 = medicationRepository.save(Medication.builder()
                                .name("Amoxicilline")
                                .therapeuticClass(TherapeuticClass.ANTIBIOTIQUE)
                                .requiresPrescription(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());

                // Add stocks
                pharmacyMedicationService.addMedicationToPharmacy(pharmacy.getId(), med1.getId(),
                                BigDecimal.valueOf(500), 50, null);
                pharmacyMedicationService.addMedicationToPharmacy(pharmacy.getId(), med2.getId(),
                                BigDecimal.valueOf(2000), 20, null);
        }

        @Test
        @WithMockUser
        void searchMedication_shouldReturnResults() throws Exception {
                mockMvc.perform(get("/api/v1/patient/search")
                                .param("query", "Doliprane")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$[0].medicationName", is("Doliprane 1000")));
        }

        @Test
        @WithMockUser
        void searchMedication_byCategory_shouldReturnResults() throws Exception {
                mockMvc.perform(get("/api/v1/patient/search")
                                .param("therapeuticClass", "ANTIBIOTIQUE")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$[0].medicationName", is("Amoxicilline")));
        }
}
