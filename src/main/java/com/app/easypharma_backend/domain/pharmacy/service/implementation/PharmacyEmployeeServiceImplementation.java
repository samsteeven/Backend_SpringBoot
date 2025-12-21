package com.app.easypharma_backend.domain.pharmacy.service.implementation;

import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import com.app.easypharma_backend.domain.pharmacy.dto.AddEmployeeRequest;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.pharmacy.service.interfaces.PharmacyEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyEmployeeServiceImplementation implements PharmacyEmployeeService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse addEmployee(UUID pharmacyId, AddEmployeeRequest request) {
        // 1. Validate inputs
        if (request.getRole() != UserRole.PHARMACY_EMPLOYEE && request.getRole() != UserRole.DELIVERY) {
            throw new IllegalArgumentException("Le rôle doit être PHARMACY_EMPLOYEE ou DELIVERY");
        }

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));

        // 2. Check if user exists or create new one
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            // Create New User
            user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .role(request.getRole())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .pharmacy(pharmacy) // Link directly
                    .isActive(true)
                    .isVerified(true) // Employees created by admins are trusted
                    .build();
        } else {
            // Update Existing User

            // Cannot hijack an Admin
            if (user.getRole() == UserRole.PHARMACY_ADMIN || user.getRole() == UserRole.SUPER_ADMIN) {
                throw new RuntimeException("Impossible d'ajouter un administrateur comme employé");
            }

            // If user already has a DIFFERENT pharmacy
            if (user.getPharmacy() != null && !user.getPharmacy().getId().equals(pharmacyId)) {
                throw new RuntimeException("L'utilisateur travaille déjà pour une autre pharmacie");
            }

            user.setRole(request.getRole());
            user.setPharmacy(pharmacy);
            user.setIsVerified(true);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void removeEmployee(UUID pharmacyId, UUID employeeId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));

        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Validate that user belongs to this pharmacy
        if (user.getPharmacy() == null || !user.getPharmacy().getId().equals(pharmacyId)) {
            throw new RuntimeException("Cet utilisateur n'est pas associé à cette pharmacie");
        }

        // Validate appropriate roles to remove (don't remove the owner via this
        // service)
        if (user.getRole() == UserRole.PHARMACY_ADMIN) {
            throw new RuntimeException("Impossible de retirer le propriétaire via ce service");
        }

        // Reset user
        user.setRole(UserRole.PATIENT);
        user.setPharmacy(null);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getEmployees(UUID pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new RuntimeException("Pharmacie non trouvée");
        }

        List<User> employees = userRepository.findByPharmacyId(pharmacyId);
        // Filter out the owner (PHARMACY_ADMIN) if strictly listing "employees"?
        // Typically owners want to see everyone. But let's filter to separate "Staff"
        // from "Owner" logic later if needed.
        // For now, return all linked users excluding SUPER_ADMIN (just in case) and
        // maybe separate owner.
        // Let's stick to returning everyone linked to pharmacy including owner, or
        // filter?
        // Prompt implies "employees". Owner is owner.
        // Let's filter to only PHARMACY_EMPLOYEE and DELIVERY.

        List<User> staff = employees.stream()
                .filter(u -> u.getRole() == UserRole.PHARMACY_EMPLOYEE || u.getRole() == UserRole.DELIVERY)
                .toList();

        return userMapper.toResponseList(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getEmployeesByRole(UUID pharmacyId, UserRole role) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new RuntimeException("Pharmacie non trouvée");
        }

        List<User> employees = userRepository.findByPharmacyIdAndRole(pharmacyId, role);
        return userMapper.toResponseList(employees);
    }
}
