package com.app.easypharma_backend.application.auth.mapper;

import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponse_ShouldMapPharmacyId_WhenPharmacyExists() {
        // Arrange
        UUID pharmacyId = UUID.randomUUID();
        String pharmacyName = "Test Pharmacy";

        Pharmacy pharmacy = Pharmacy.builder()
                .id(pharmacyId)
                .name(pharmacyName)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .pharmacy(pharmacy)
                .build();

        // Act
        UserResponse response = userMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(pharmacyId, response.getPharmacyId());
        assertEquals(pharmacyName, response.getPharmacyName());
    }
}
