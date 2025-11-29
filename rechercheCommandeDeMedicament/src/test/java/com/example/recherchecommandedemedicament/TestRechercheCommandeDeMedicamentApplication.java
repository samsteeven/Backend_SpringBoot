package com.example.recherchecommandedemedicament;

import org.springframework.boot.SpringApplication;

public class TestRechercheCommandeDeMedicamentApplication {

    public static void main(String[] args) {
        SpringApplication.from(RechercheCommandeDeMedicamentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
