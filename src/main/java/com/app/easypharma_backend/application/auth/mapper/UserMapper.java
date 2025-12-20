package com.app.easypharma_backend.application.auth.mapper;

import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.domain.auth.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Convertit une entité User en UserResponse
     * Exclut le mot de passe pour des raisons de sécurité
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "isVerified", source = "isVerified")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "pharmacyId", expression = "java(user.getPharmacy() != null ? user.getPharmacy().getId() : null)")
    @Mapping(target = "pharmacyName", expression = "java(user.getPharmacy() != null ? user.getPharmacy().getName() : null)")
    UserResponse toResponse(User user);

    /**
     * Convertit une liste d'entités User en liste de UserResponse
     */
    List<UserResponse> toResponseList(List<User> users);

    /**
     * Convertit un RegisterRequest en entité User
     * Ignore les champs qui seront définis par la logique métier
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Hash géré par le Use Case
    @Mapping(target = "pharmacy", ignore = true) // Géré par le Use Case
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true) // Géré par @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Géré par @PrePersist
    User toEntity(RegisterRequest request);

    /**
     * Met à jour une entité User existante avec les données d'un RegisterRequest
     * Utilise pour les mises à jour partielles
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(RegisterRequest request, @MappingTarget User user);

    /**
     * Met à jour une entité User existante avec les données d'un UpdateUserRequest
     * Utilise pour les mises à jour partielles du profil utilisateur
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);

    /**
     * Mapping personnalisé pour les cas spéciaux
     * Par exemple, concatenation du nom complet
     */
    @Named("getFullName")
    default String getFullName(User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }
}