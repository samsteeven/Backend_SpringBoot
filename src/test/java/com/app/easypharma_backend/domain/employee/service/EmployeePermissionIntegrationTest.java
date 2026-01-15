package com.app.easypharma_backend.domain.employee.service;

import com.app.easypharma_backend.config.TestMailConfiguration;
import com.app.easypharma_backend.domain.employee.dto.EmployeePermissionDTO;
import com.app.easypharma_backend.domain.employee.entity.EmployeePermission;
import com.app.easypharma_backend.domain.employee.repository.EmployeePermissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestMailConfiguration.class)
@ActiveProfiles("test")
@Transactional
public class EmployeePermissionIntegrationTest {

    @Autowired
    private EmployeePermissionService permissionService;

    @Autowired
    private EmployeePermissionRepository permissionRepository;

    @Test
    public void shouldCreateDefaultPermissionsWhenNoneExist() {
        UUID employeeId = UUID.randomUUID();

        EmployeePermissionDTO permissions = permissionService.getPermissions(employeeId);

        assertThat(permissions).isNotNull();
        assertThat(permissions.getEmployeeId()).isEqualTo(employeeId);
        assertThat(permissions.getCanPrepareOrders()).isTrue();
        assertThat(permissions.getCanViewStatistics()).isFalse();

        assertThat(permissionRepository.existsByEmployeeId(employeeId)).isTrue();
    }

    @Test
    public void shouldUpdatePermissions() {
        UUID employeeId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        permissionService.getPermissions(employeeId); // Create default

        EmployeePermissionDTO updateDto = EmployeePermissionDTO.builder()
                .canPrepareOrders(false)
                .canAssignDeliveries(true)
                .canViewStatistics(true)
                .canManageInventory(false)
                .canViewCustomerInfo(true)
                .canProcessPayments(true)
                .build();

        EmployeePermissionDTO updated = permissionService.updatePermissions(employeeId, updateDto, adminId);

        assertThat(updated.getCanPrepareOrders()).isFalse();
        assertThat(updated.getCanViewStatistics()).isTrue();

        EmployeePermission entity = permissionRepository.findByEmployeeId(employeeId).orElseThrow();
        assertThat(entity.getLastModifiedBy()).isEqualTo(adminId);
    }
}
