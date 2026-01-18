package com.app.easypharma_backend.domain.pharmacy.service;

import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDashboardDTO;
import com.app.easypharma_backend.domain.pharmacy.dto.RevenueDataDTO;
import com.app.easypharma_backend.domain.pharmacy.dto.TopProductDTO;
import com.app.easypharma_backend.domain.medication.repository.PharmacyMedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyDashboardService {

    private final OrderRepository orderRepository;
    private final PharmacyMedicationRepository pharmacyMedicationRepository;

    public PharmacyDashboardDTO getDashboardStats(UUID pharmacyId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 1. Basic Stats
        long totalOrders = orderRepository.countTotalOrdersByPharmacy(pharmacyId);
        long pendingOrders = orderRepository.countPendingOrdersByPharmacy(pharmacyId);
        BigDecimal monthlyRevenue = orderRepository.calculateRevenueByPharmacySince(pharmacyId, thirtyDaysAgo);
        if (monthlyRevenue == null)
            monthlyRevenue = BigDecimal.ZERO;

        // 2. Inventory Stats (Reuse logic or simple queries)
        // lowStockCount threshold = 5
        int lowStockCount = (int) pharmacyMedicationRepository.countLowStock(pharmacyId, 5);

        // expiringSoon threshold = 90 days
        LocalDateTime ninetyDaysFromNow = LocalDateTime.now().plusDays(90);
        int expiringSoonCount = (int) pharmacyMedicationRepository.countExpiringSoon(pharmacyId,
                ninetyDaysFromNow.toLocalDate());

        // 3. Revenue Evolution (Chart 1 - Line)
        List<Object[]> revenueRows = orderRepository.findDailyRevenueByPharmacy(pharmacyId, thirtyDaysAgo);
        List<RevenueDataDTO> revenueEvolution = revenueRows.stream()
                .map(row -> RevenueDataDTO.builder()
                        .date(row[0].toString())
                        .amount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());

        // 4. Top Products (Chart 2 - Bar)
        List<Object[]> topRows = orderRepository.findTopSellingMedicationsByPharmacy(pharmacyId, PageRequest.of(0, 5));
        List<TopProductDTO> topProducts = topRows.stream()
                .map(row -> TopProductDTO.builder()
                        .name((String) row[0])
                        .count((long) row[1])
                        .build())
                .collect(Collectors.toList());

        // 5. Order Status Distribution (Chart 3 - Doughnut)
        List<Object[]> statusRows = orderRepository.countOrdersByStatusByPharmacy(pharmacyId);
        Map<String, Long> statusDistribution = statusRows.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]));

        return PharmacyDashboardDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .activeDeliveries(0) // TODO: Link to DeliveryRepository if available
                .monthlyRevenue(monthlyRevenue)
                .lowStockCount(lowStockCount)
                .expiringSoonCount(expiringSoonCount)
                .revenueEvolution(revenueEvolution)
                .topProducts(topProducts)
                .orderStatusDistribution(statusDistribution)
                .build();
    }
}
