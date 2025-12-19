package com.app.easypharma_backend.domain.medication.mapper;

import com.app.easypharma_backend.domain.medication.dto.PharmacyMedicationDTO;
import com.app.easypharma_backend.domain.medication.entity.PharmacyMedication;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        MedicationMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PharmacyMedicationMapper {

    @Mapping(target = "pharmacyId", source = "pharmacy.id")
    @Mapping(target = "medicationId", source = "medication.id")
    PharmacyMedicationDTO toDTO(PharmacyMedication entity);

    List<PharmacyMedicationDTO> toDTOList(List<PharmacyMedication> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pharmacy", ignore = true) // Managed by service
    @Mapping(target = "medication", ignore = true) // Managed by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PharmacyMedication toEntity(PharmacyMedicationDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pharmacy", ignore = true)
    @Mapping(target = "medication", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(PharmacyMedicationDTO dto, @MappingTarget PharmacyMedication entity);
}
