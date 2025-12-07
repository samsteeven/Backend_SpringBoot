package com.app.easypharma_backend;

import org.springframework.boot.SpringApplication;

public class TestEasypharmaApplication {

    public static void main(String[] args) {
        SpringApplication.from(EasypharmaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
