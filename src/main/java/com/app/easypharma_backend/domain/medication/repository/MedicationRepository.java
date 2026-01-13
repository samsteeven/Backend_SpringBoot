package com.app.easypharma_backend.domain.medication.repository;

import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    // Recherche par nom exact
    Optional<Medication> findByName(String name);

    // Recherche par nom et dosage
    Optional<Medication> findByNameAndDosage(String name, String dosage);

    // Recherche par classe thérapeutique
    List<Medication> findByTherapeuticClass(TherapeuticClass therapeuticClass);

    // Recherche médicaments nécessitant ordonnance
    List<Medication> findByRequiresPrescriptionTrue();

    // Recherche par nom (LIKE, case insensitive)
    @Query(value = "SELECT * FROM medications WHERE name ILIKE '%' || CAST(:query AS text) || '%'", nativeQuery = true)
    List<Medication> searchByName(@Param("query") String query);

    // Recherche full-text (nom ou nom générique)
    @Query(value = """
            SELECT * FROM medications
            WHERE name ILIKE '%' || CAST(:query AS text) || '%'
            OR generic_name ILIKE '%' || CAST(:query AS text) || '%'
            """, nativeQuery = true)
    List<Medication> searchByNameOrGenericName(@Param("query") String query);

    // Recherche avec filtres multiples (Requête Native pour compatibilité
    // PostgreSQL stricte)
    @Query(value = """
            SELECT * FROM medications
            WHERE (CAST(:name AS text) IS NULL OR name ILIKE '%' || CAST(:name AS text) || '%')
            AND (CAST(:therapeuticClass AS text) IS NULL OR therapeutic_class = CAST(:therapeuticClass AS text))
            AND (:requiresPrescription IS NULL OR requires_prescription = :requiresPrescription)
            ORDER BY name ASC
            """, nativeQuery = true)
    List<Medication> searchWithFilters(
            @Param("name") String name,
            @Param("therapeuticClass") String therapeuticClass,
            @Param("requiresPrescription") Boolean requiresPrescription);
}