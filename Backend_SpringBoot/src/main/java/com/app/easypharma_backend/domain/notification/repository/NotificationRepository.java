package com.app.easypharma_backend.domain.notification.repository;

import com.app.easypharma_backend.domain.notification.entity.Notification;
import com.app.easypharma_backend.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Recherche par utilisateur
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Recherche par utilisateur avec pagination
    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    // Recherche notifications non lues
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    // Recherche par type
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    // Recherche par utilisateur et type
    List<Notification> findByUserIdAndType(UUID userId, NotificationType type);

    // Compter notifications non lues
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") UUID userId);

    // Marquer toutes comme lues
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    // Supprimer notifications anciennes
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Supprimer notifications lues anciennes
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}