package com.app.easypharma_backend.domain.medication.mapper;

import com.app.easypharma_backend.domain.medication.dto.MedicationDTO;
import com.app.easypharma_backend.domain.medication.entity.Medication;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MedicationMapper {
    MedicationDTO toDTO(Medication medication);

    List<MedicationDTO> toDTOList(List<Medication> medications);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Medication toEntity(MedicationDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(MedicationDTO dto, @MappingTarget Medication medication);
}
