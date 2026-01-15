package com.app.easypharma_backend.integration;

import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import com.app.easypharma_backend.domain.medication.repository.MedicationRepository;
import com.app.easypharma_backend.domain.medication.service.interfaces.PharmacyMedicationServiceInterface;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PatientScenarioTest {

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

    @Autowired
    private com.app.easypharma_backend.domain.order.repository.OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User patient;
    private Pharmacy pharmacy;
    private Medication medication;

    @BeforeEach
    void setup() {
        // 1. Pharmacist & Pharmacy
        User pharmacist = userRepository.save(User.builder()
                .email("pharma_scen@test.com")
                .password("test")
                .firstName("Pharma")
                .lastName("Scen")
                .phone("999999999")
                .role(UserRole.PHARMACY_ADMIN)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        pharmacy = pharmacyRepository.save(Pharmacy.builder()
                .user(pharmacist)
                .name("Pharmacie Scenario")
                .licenseNumber("LIC-SCEN")
                .address("Akwa")
                .city("Douala")
                .phone("888888888")
                .latitude(BigDecimal.valueOf(4.05))
                .longitude(BigDecimal.valueOf(9.70))
                .status(PharmacyStatus.APPROVED)
                .licenseDocumentUrl("http://doc.url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 2. Medication
        medication = medicationRepository.save(Medication.builder()
                .name("Doliprane 500")
                .therapeuticClass(TherapeuticClass.ANTALGIQUE)
                .requiresPrescription(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        pharmacyMedicationService.addMedicationToPharmacy(pharmacy.getId(), medication.getId(), BigDecimal.valueOf(100),
                50, null);

        // 3. Patient
        patient = userRepository.save(User.builder()
                .email("patient_scen@test.com")
                .password("test")
                .firstName("Patient")
                .lastName("Scen")
                .phone("777777777")
                .role(UserRole.PATIENT)
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @WithMockUser(username = "patient_scen@test.com", roles = { "PATIENT" })
    void testFullPatientFlow() throws Exception {
        // --- 1. Search ---
        mockMvc.perform(get("/api/v1/patient/search")
                .param("query", "Doliprane")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicationName", containsString("Doliprane")));

        // --- 2. Create Order ---
        // Payload for CreateOrderDTO
        Object orderItem = new java.util.HashMap<String, Object>() {
            {
                put("medicationId", medication.getId().toString());
                put("quantity", 2);
            }
        };

        Object createOrderPayload = new java.util.HashMap<String, Object>() {
            {
                put("pharmacyId", pharmacy.getId().toString());
                put("items", Collections.singletonList(orderItem));
                put("deliveryAddress", "Bonanjo");
                put("deliveryCity", "Douala");
                put("deliveryPhone", "777777777");
            }
        };

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andReturn();

        String orderId = com.jayway.jsonpath.JsonPath.read(orderResult.getResponse().getContentAsString(), "$.id");

        // --- 3. Payment ---
        Object paymentPayload = new java.util.HashMap<String, Object>() {
            {
                put("orderId", orderId);
                put("method", "MTN_MOMO");
                put("phoneNumber", "670000000");
            }
        };

        // --- 2.5 Manually Confirm Order (Required before Payment) ---
        com.app.easypharma_backend.domain.order.entity.Order orderToConfirm = orderRepository
                .findById(java.util.UUID.fromString(orderId)).orElseThrow();
        orderToConfirm.setStatus(com.app.easypharma_backend.domain.order.entity.OrderStatus.CONFIRMED);
        orderRepository.save(orderToConfirm);

        mockMvc.perform(post("/api/v1/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")));

        // Verify Order is PAID
        mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAID")));

        // --- 3.5 Manually set Order to DELIVERED (Simulate Delivery) ---
        // In a real e2e test we would have the courier deliver it, but here we focus on
        // Patient flow.
        com.app.easypharma_backend.domain.order.entity.Order savedOrder = orderRepository
                .findById(java.util.UUID.fromString(orderId)).orElseThrow();
        savedOrder.setStatus(com.app.easypharma_backend.domain.order.entity.OrderStatus.DELIVERED);
        orderRepository.save(savedOrder);

        // --- 4. Review ---
        Object reviewPayload = new java.util.HashMap<String, Object>() {
            {
                put("orderId", orderId);
                put("pharmacyRating", 5);
                put("pharmacyComment", "Top service");
            }
        };

        mockMvc.perform(post("/api/v1/reviews")
                // Need to ensure the order is DELIVERED before review allowed?
                // Typically yes, but let's check Service logic.
                // If it fails, I might need to manually update status to DELIVERED first
                // testing hack.
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewPayload)))
                .andExpect(status().isOk());
    }
}
