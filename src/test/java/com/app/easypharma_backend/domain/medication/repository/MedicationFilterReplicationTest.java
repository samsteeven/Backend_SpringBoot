package com.app.easypharma_backend.domain.medication.repository;

import com.app.easypharma_backend.config.TestMailConfiguration;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import com.app.easypharma_backend.domain.medication.entity.TherapeuticClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestMailConfiguration.class)
@ActiveProfiles("test")
public class MedicationFilterReplicationTest {

    @Autowired
    private MedicationRepository medicationRepository;

    @Test
    void testFilterByRequiresPrescriptionOnly() {
        Medication med = Medication.builder()
                .name("Paracetamol")
                .therapeuticClass(TherapeuticClass.ANTALGIQUE)
                .requiresPrescription(true)
                .build();
        medicationRepository.save(med);

        // This is what the controller calls
        List<Medication> results = medicationRepository.searchWithFilters(null, null, true);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getRequiresPrescription()).isTrue();
    }
}
