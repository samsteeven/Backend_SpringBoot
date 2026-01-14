package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.admin.dto.GlobalStatsDTO;
import com.app.easypharma_backend.domain.admin.dto.TopMedicationDTO;
import com.app.easypharma_backend.domain.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Statistiques et métriques globales pour le Super Admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final com.app.easypharma_backend.domain.employee.service.EmployeeReportService reportService;

    @Operation(summary = "Statistiques Globales", description = "Revenus, volume de commandes, pharmacies actives")
    @GetMapping("/stats")
    public ResponseEntity<GlobalStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(adminDashboardService.getGlobalStats());
    }

    @Operation(summary = "Top Médicaments Vendus", description = "Liste des médicaments les plus commandés")
    @GetMapping("/top-medications/sold")
    public ResponseEntity<List<TopMedicationDTO>> getTopSold() {
        return ResponseEntity.ok(adminDashboardService.getTopSellingMedications());
    }

    @Operation(summary = "Top Recherches", description = "Liste des termes les plus recherchés")
    @GetMapping("/top-medications/searched")
    public ResponseEntity<List<TopMedicationDTO>> getTopSearched() {
        return ResponseEntity.ok(adminDashboardService.getTopSearchedMedications());
    }

    @Operation(summary = "Exporter Rapport Global", description = "Générer un fichier CSV des statistiques globales")
    @GetMapping("/report")
    public void exportGlobalReport(
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) @org.springframework.web.bind.annotation.RequestParam(required = false) java.time.LocalDate startDate,
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) @org.springframework.web.bind.annotation.RequestParam(required = false) java.time.LocalDate endDate,
            HttpServletResponse response) throws IOException {

        if (startDate == null)
            startDate = java.time.LocalDate.now().minusMonths(1);
        if (endDate == null)
            endDate = java.time.LocalDate.now();

        byte[] csvData = reportService.generateGlobalAdminReport(startDate, endDate);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=global-admin-report.csv");
        response.getOutputStream().write(csvData);
    }
}
