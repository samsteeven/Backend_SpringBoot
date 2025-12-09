package com.app.easypharma_backend.domain.auth.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserRole {
    @JsonProperty("PATIENT")
    PATIENT,
    
    @JsonProperty("PHARMACIST")
    PHARMACIST,
    
    @JsonProperty("DELIVERY")
    DELIVERY,
    
    @JsonProperty("ADMIN")
    ADMIN
}