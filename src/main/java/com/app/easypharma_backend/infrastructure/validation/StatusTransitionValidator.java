package com.app.easypharma_backend.infrastructure.validation;

import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validator for order status transitions
 * Ensures that status changes follow the correct workflow
 */
@Component
public class StatusTransitionValidator {

    /**
     * Map of allowed transitions for each status
     */
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, EnumSet.of(OrderStatus.IN_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.IN_DELIVERY, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class), // Terminal state
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class) // Terminal state
    );

    /**
     * Validate if a status transition is allowed
     */
    public boolean isTransitionAllowed(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        if (currentStatus == newStatus) {
            return true; // Same status is always allowed
        }

        Set<OrderStatus> allowedStatuses = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedStatuses != null && allowedStatuses.contains(newStatus);
    }

    /**
     * Validate transition and throw exception if invalid
     */
    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!isTransitionAllowed(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s. Allowed transitions: %s",
                            currentStatus, newStatus, ALLOWED_TRANSITIONS.get(currentStatus)));
        }
    }

    /**
     * Get allowed next statuses for a given status
     */
    public Set<OrderStatus> getAllowedNextStatuses(OrderStatus currentStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(OrderStatus.class));
    }

    /**
     * Check if a status is terminal (no further transitions allowed)
     */
    public boolean isTerminalStatus(OrderStatus status) {
        Set<OrderStatus> allowedStatuses = ALLOWED_TRANSITIONS.get(status);
        return allowedStatuses == null || allowedStatuses.isEmpty();
    }
}
