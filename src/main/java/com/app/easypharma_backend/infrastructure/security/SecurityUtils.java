package com.app.easypharma_backend.infrastructure.security;

import com.app.easypharma_backend.domain.auth.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityUtils {

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().map(User::getId).orElse(null);
    }

    public static String getCurrentUsername() {
        return getCurrentUser().map(User::getEmail).orElse("System");
    }
}
