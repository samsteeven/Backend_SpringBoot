package com.app.easypharma_backend.domain.auth.repository;


import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Recherche par email
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    // Recherche par téléphone
    Optional<User> findByPhone(String phone);

    // Vérifier si email existe
    boolean existsByEmail(String email);

    // Vérifier si téléphone existe
    boolean existsByPhone(String phone);

    // Recherche par rôle
    List<User> findByRole(UserRole role);

    // Recherche par rôle et statut actif
    List<User> findByRoleAndIsActiveTrue(UserRole role);

    // Recherche utilisateurs actifs
    List<User> findByIsActiveTrue();

    // Recherche utilisateurs par ville
    List<User> findByCity(String city);

    // Recherche utilisateurs avec coordonnées GPS
    @Query("SELECT u FROM User u WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL")
    List<User> findUsersWithLocation();

    // Recherche livreurs disponibles dans une ville
    @Query("SELECT u FROM User u WHERE u.role = 'DELIVERY' AND u.isActive = true AND u.city = :city")
    List<User> findAvailableDeliveryPersonsInCity(@Param("city") String city);
}