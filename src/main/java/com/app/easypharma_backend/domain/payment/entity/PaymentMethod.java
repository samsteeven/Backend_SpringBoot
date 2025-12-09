package com.app.easypharma_backend.domain.payment.entity;

public enum PaymentMethod {
    MTN_MOMO("MTN Mobile Money"),
    ORANGE_MONEY("Orange Money"),
    CASH("Paiement en espèces");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}