package com.app.easypharma_backend.domain.medication.entity;

import lombok.Getter;

@Getter
public enum TherapeuticClass {
    ANTALGIQUE("Antalgique"),
    ANTIBIOTIQUE("Antibiotique"),
    ANTIPALUDEEN("Antipaludéen"),
    ANTIHYPERTENSEUR("Antihypertenseur"),
    ANTIINFLAMMATOIRE("Antiinflammatoire"),
    ANTIDIABETIQUE("Antidiabétique"),
    VITAMINE("Vitamine"),
    AUTRE("Autre");

    private final String displayName;

    TherapeuticClass(String displayName) {
        this.displayName = displayName;
    }

}