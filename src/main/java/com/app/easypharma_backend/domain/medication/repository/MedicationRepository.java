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
    @Query("SELECT m FROM Medication m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Medication> searchByName(@Param("query") String query);

    // Recherche full-text (nom ou nom générique)
    @Query("""
        SELECT m FROM Medication m 
        WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    List<Medication> searchByNameOrGenericName(@Param("query") String query);

    // Recherche avec filtres multiples
    @Query("""
        SELECT DISTINCT m FROM Medication m
        WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:therapeuticClass IS NULL OR m.therapeuticClass = :therapeuticClass)
        AND (:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription)
        ORDER BY m.name ASC
        """)
    List<Medication> searchWithFilters(
            @Param("name") String name,
            @Param("therapeuticClass") TherapeuticClass therapeuticClass,
            @Param("requiresPrescription") Boolean requiresPrescription
    );
}