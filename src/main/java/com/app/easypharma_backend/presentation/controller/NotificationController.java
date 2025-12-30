package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.domain.notification.dto.NotificationDTO;
import com.app.easypharma_backend.domain.notification.entity.Notification;
import com.app.easypharma_backend.domain.notification.repository.NotificationRepository;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Consultation des notifications in-app")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Lister mes notifications", description = "Récupère les notifications in-app de l'utilisateur connecté")
    @GetMapping("/my-notifications")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        UUID userId = getCurrentUserId();
        List<NotificationDTO> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Marquer comme lu", description = "Marque une notification spécifique comme lue")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable @NonNull UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        if (!notification.getUser().getId().equals(getCurrentUserId())) {
            throw new RuntimeException("Accès non autorisé");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
