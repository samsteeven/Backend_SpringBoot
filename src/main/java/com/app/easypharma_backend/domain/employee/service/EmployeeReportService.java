package com.app.easypharma_backend.domain.employee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for generating employee reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeReportService {

    /**
     * Generate CSV report for an employee
     */
    public byte[] generateEmployeeReport(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(outputStream)) {

            // CSV Header
            writer.println("MEDICAM - Rapport d'Activité Employé");
            writer.println("Période: " + formatDate(startDate) + " - " + formatDate(endDate));
            writer.println();
            writer.println("Métrique,Valeur");

            // TODO: Fetch actual data from database
            // For now, using placeholder data
            writer.println("Commandes Traitées (Aujourd'hui),0");
            writer.println("Commandes Traitées (Cette Semaine),0");
            writer.println("Commandes Traitées (Ce Mois),0");
            writer.println("Livraisons Assignées (Aujourd'hui),0");
            writer.println("Livraisons Assignées (Cette Semaine),0");
            writer.println("Livraisons Assignées (Ce Mois),0");
            writer.println("Temps Moyen de Traitement (minutes),0.0");

            writer.flush();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating employee report", e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Generate CSV report for all employees in a pharmacy
     */
    public byte[] generatePharmacyEmployeesReport(UUID pharmacyId, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(outputStream)) {

            // CSV Header
            writer.println("MEDICAM - Rapport d'Activité des Employés");
            writer.println("Période: " + formatDate(startDate) + " - " + formatDate(endDate));
            writer.println();

            // Column headers
            writer.println(
                    "Employé,Email,Commandes (Jour),Commandes (Semaine),Commandes (Mois),Livraisons (Jour),Livraisons (Semaine),Livraisons (Mois),Temps Moyen (min)");

            // TODO: Fetch actual employee data
            // For now, placeholder
            writer.println("Exemple Employé,employee@example.com,0,0,0,0,0,0,0.0");

            writer.flush();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating pharmacy employees report", e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Generate CSV report for Global Admin
     */
    public byte[] generateGlobalAdminReport(LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(outputStream)) {

            // CSV Header
            writer.println("MEDICAM - Rapport Global d'Administration");
            writer.println("Période: " + formatDate(startDate) + " - " + formatDate(endDate));
            writer.println();

            // Stats
            writer.println("Métrique,Valeur");
            writer.println("Revenu Global Total,0");
            writer.println("Volume Global de Commandes,0");
            writer.println("Pharmacies Actives,0");
            writer.println("Nouveaux Inscrits (Période),0");

            writer.flush();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating global admin report", e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
