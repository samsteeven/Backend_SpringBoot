package com.app.easypharma_backend.domain.admin.service;

import com.app.easypharma_backend.domain.admin.dto.GlobalStatsDTO;
import com.app.easypharma_backend.domain.admin.dto.TopMedicationDTO;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.search.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final SearchLogRepository searchLogRepository;

    public GlobalStatsDTO getGlobalStats() {
        return GlobalStatsDTO.builder()
                .totalOrders(orderRepository.countTotalOrders())
                .pendingOrders(orderRepository.countPendingOrders())
                .totalRevenue(
                        orderRepository.calculateGlobalRevenue() != null ? orderRepository.calculateGlobalRevenue()
                                : BigDecimal.ZERO)
                .activePharmacies(pharmacyRepository
                        .countByStatus(com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus.APPROVED))
                .totalPatients(userRepository.countByRole(UserRole.PATIENT))
                .build();
    }

    public List<TopMedicationDTO> getTopSellingMedications() {
        List<Object[]> results = orderRepository.findTopSellingMedications(PageRequest.of(0, 10));
        return results.stream()
                .map(row -> TopMedicationDTO.builder()
                        .name((String) row[0])
                        .count((long) row[1])
                        .type("SOLD")
                        .build())
                .collect(Collectors.toList());
    }

    public List<TopMedicationDTO> getTopSearchedMedications() {
        List<Object[]> results = searchLogRepository.findTopSearchedQueries(PageRequest.of(0, 10));
        return results.stream()
                .map(row -> TopMedicationDTO.builder()
                        .name((String) row[0])
                        .count((long) row[1])
                        .type("SEARCHED")
                        .build())
                .collect(Collectors.toList());
    }
}
