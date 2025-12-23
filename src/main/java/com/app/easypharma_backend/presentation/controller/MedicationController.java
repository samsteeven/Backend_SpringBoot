package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import com.app.easypharma_backend.domain.medication.repository.MedicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
@Tag(name = "Medication Catalog", description = "Catalogue global des médicaments disponibles")
public class MedicationController {

    private final MedicationRepository medicationRepository;

    @Operation(summary = "Lister tous les médicaments", description = "Retourne la liste complète des médicaments du catalogue (pour PHARMACY_ADMIN qui veut ajouter à son inventaire)")
    @GetMapping
    public ResponseEntity<List<Medication>> getAllMedications() {
        return ResponseEntity.ok(medicationRepository.findAll());
    }

    @Operation(summary = "Créer un nouveau médicament", description = "Permet au PHARMACY_ADMIN de créer un médicament s'il n'existe pas dans le catalogue")
    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Medication> createMedication(@RequestBody @NonNull CreateMedicationRequest request) {
        // Vérifier si le médicament existe déjà
        if (request.getName() != null && request.getDosage() != null) {
            var existing = medicationRepository.findByNameAndDosage(request.getName(), request.getDosage());
            if (existing.isPresent()) {
                return ResponseEntity.ok(existing.get()); // Retourner le médicament existant
            }
        }

        // Créer le nouveau médicament
        Medication medication = Medication.builder()
                .name(request.getName())
                .genericName(request.getGenericName())
                .therapeuticClass(request.getTherapeuticClass())
                .description(request.getDescription())
                .dosage(request.getDosage())
                .requiresPrescription(
                        request.getRequiresPrescription() != null ? request.getRequiresPrescription() : false)
                .build();

        Medication saved = medicationRepository.save(medication);
        return ResponseEntity.status(201).body(saved);
    }

    @Operation(summary = "Rechercher des médicaments", description = "Recherche par nom ou nom générique (insensible à la casse)")
    @GetMapping("/search")
    public ResponseEntity<List<Medication>> searchMedications(
            @Parameter(description = "Terme de recherche") @RequestParam @NonNull String query) {
        return ResponseEntity.ok(medicationRepository.searchByNameOrGenericName(query));
    }

    @Operation(summary = "Filtrer les médicaments", description = "Recherche avancée avec filtres multiples")
    @GetMapping("/filter")
    public ResponseEntity<List<Medication>> filterMedications(
            @Parameter(description = "Nom du médicament (partiel)") @RequestParam(required = false) String name,
            @Parameter(description = "Classe thérapeutique") @RequestParam(required = false) TherapeuticClass therapeuticClass,
            @Parameter(description = "Nécessite ordonnance") @RequestParam(required = false) Boolean requiresPrescription) {
        return ResponseEntity.ok(
                medicationRepository.searchWithFilters(name, therapeuticClass, requiresPrescription));
    }

    @Operation(summary = "Obtenir un médicament par ID", description = "Récupère les détails d'un médicament spécifique")
    @GetMapping("/{id}")
    public ResponseEntity<Medication> getMedicationById(
            @Parameter(description = "ID du médicament") @PathVariable @NonNull UUID id) {
        return medicationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Médicaments par classe thérapeutique", description = "Liste les médicaments d'une classe thérapeutique donnée")
    @GetMapping("/by-class/{therapeuticClass}")
    public ResponseEntity<List<Medication>> getMedicationsByClass(
            @Parameter(description = "Classe thérapeutique") @PathVariable @NonNull TherapeuticClass therapeuticClass) {
        return ResponseEntity.ok(medicationRepository.findByTherapeuticClass(therapeuticClass));
    }

    @Operation(summary = "Médicaments nécessitant ordonnance", description = "Liste uniquement les médicaments qui nécessitent une ordonnance")
    @GetMapping("/prescription-required")
    public ResponseEntity<List<Medication>> getMedicationsRequiringPrescription() {
        return ResponseEntity.ok(medicationRepository.findByRequiresPrescriptionTrue());
    }
}
