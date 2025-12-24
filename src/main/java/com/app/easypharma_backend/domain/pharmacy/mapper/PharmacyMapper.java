package com.app.easypharma_backend.domain.pharmacy.mapper;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PharmacyMapper {

    /**
     * Convertit une entité Pharmacy en PharmacyDTO
     * Extrait userId de la relation User
     */
    @Mapping(target = "userId", expression = "java(pharmacy.getUser() != null ? pharmacy.getUser().getId() : null)")
    PharmacyDTO toDTO(Pharmacy pharmacy);

    /**
     * Convertit une liste d'entités Pharmacy en liste de PharmacyDTO
     */
    List<PharmacyDTO> toDTOList(List<Pharmacy> pharmacies);

    /**
     * Convertit un PharmacyDTO en entité Pharmacy (pour création)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Géré par la logique métier
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true) // Géré par @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Géré par @PrePersist/@PreUpdate
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    Pharmacy toEntity(PharmacyDTO dto);

    /**
     * Met à jour une entité Pharmacy existante avec les données d'un PharmacyDTO
     * Mises à jour partielles
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    void updateEntityFromDTO(PharmacyDTO dto, @MappingTarget Pharmacy pharmacy);
}