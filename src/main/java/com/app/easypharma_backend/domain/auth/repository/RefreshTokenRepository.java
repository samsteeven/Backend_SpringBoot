package com.app.easypharma_backend.domain.auth.repository;


import com.app.easypharma_backend.domain.auth.entity.RefreshToken;
import com.app.easypharma_backend.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Recherche par token
    Optional<RefreshToken> findByToken(String token);

    // Recherche par utilisateur
    Optional<RefreshToken> findByUser(User user);

    // Supprimer tokens expirés
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    // Supprimer tous les tokens d'un utilisateur
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(User user);
}
